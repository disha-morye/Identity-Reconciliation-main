package com.example.Identity.Reconciliation.service;

import com.example.Identity.Reconciliation.DTO.CustomerRequestDTO;
import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.entity.CustomerEntity;
import com.example.Identity.Reconciliation.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomerService {

    private CustomerRepository repo;

    @Transactional
    public ResponseEntity<CustomerResponseDTO> identifyCustomerContact(CustomerRequestDTO customerRequestDTO) {

        String email = customerRequestDTO.getEmail();
        String phoneNumber = customerRequestDTO.getPhoneNumber();

        List<CustomerEntity> emailMatches = email == null ? Collections.emptyList() : repo.findByEmail(email);
        List<CustomerEntity> phoneMatches = phoneNumber == null ? Collections.emptyList() : repo.findByPhoneNumber(phoneNumber);

        // --- CASE 1: New unique identity (no match for email/phone)
        if (emailMatches.isEmpty() && phoneMatches.isEmpty()) {
            CustomerEntity newPrimary = new CustomerEntity();
            newPrimary.setEmail(email);
            newPrimary.setPhoneNumber(phoneNumber);
            newPrimary.setLinkedPreference("primary");
            // createdAt, updatedAt handled by @PrePersist
            repo.save(newPrimary);
            return ResponseEntity.ok(formatResponse(newPrimary, List.of()));
        }

        // Find all the roots for the email and phone matches (if any)
        CustomerEntity rootPrimary = null;
        if (!emailMatches.isEmpty() && !phoneMatches.isEmpty()) {
            // Could be different clusters, resolve each to root, for potential merge
            CustomerEntity rootA = findRoot(emailMatches.get(0));
            CustomerEntity rootB = findRoot(phoneMatches.get(0));

            if (!rootA.getId().equals(rootB.getId())) {
                // Merge two trees, oldest as primary
                CustomerEntity winner = rootA.getCreatedAt().isBefore(rootB.getCreatedAt()) ? rootA : rootB;
                CustomerEntity loser = winner == rootA ? rootB : rootA;
                demoteTreeToSecondary(loser, winner.getId());
                rootPrimary = winner;
            } else {
                rootPrimary = rootA;
            }
        } else {
            // Only one side matches, climb up to the root
            rootPrimary = !emailMatches.isEmpty() ? findRoot(emailMatches.get(0)) : findRoot(phoneMatches.get(0));
        }

        // Check if exact match exists (`both fields match the same customer`)
        boolean alreadyExists = Stream.concat(emailMatches.stream(), phoneMatches.stream())
                .anyMatch(e -> Objects.equals(e.getEmail(), email) && Objects.equals(e.getPhoneNumber(), phoneNumber));

        // --- CASE 3: Both fields match same customer, no creation, just return
        if (alreadyExists) {
            List<CustomerEntity> allCluster = collectAllLinked(rootPrimary);
            return ResponseEntity.ok(formatResponse(rootPrimary, allCluster));
        }

        // --- CASE 2: Only one field matches (partial match), add as secondary
        if ((emailMatches.isEmpty() && !phoneMatches.isEmpty()) || (!emailMatches.isEmpty() && phoneMatches.isEmpty())) {
            CustomerEntity newSec = new CustomerEntity();
            newSec.setEmail(email);
            newSec.setPhoneNumber(phoneNumber);
            newSec.setLinkedPreference("secondary");
            newSec.setLinkedId(rootPrimary.getId());
            repo.save(newSec);

            List<CustomerEntity> allCluster = collectAllLinked(rootPrimary);
            return ResponseEntity.ok(formatResponse(rootPrimary, allCluster));
        }

        // --- CASE 4: Both fields match but different clusters, handled above (merge)... now just return merged cluster
        List<CustomerEntity> allCluster = collectAllLinked(rootPrimary);
        return ResponseEntity.ok(formatResponse(rootPrimary, allCluster));
    }

    // Upward traversal to the root primary customer
    private CustomerEntity findRoot(CustomerEntity customer) {
        while (customer.getLinkedId() != null) {
            Optional<CustomerEntity> parent = repo.findById(customer.getLinkedId());
            if (parent.isEmpty()) break;
            customer = parent.get();
        }
        return customer;
    }

    // Downward traversal: get all customers in this cluster (BFS)
    private List<CustomerEntity> collectAllLinked(CustomerEntity root) {
        List<CustomerEntity> result = new ArrayList<>();
        Queue<CustomerEntity> queue = new LinkedList<>();
        result.add(root);
        queue.add(root);

        while (!queue.isEmpty()) {
            CustomerEntity curr = queue.poll();
            List<CustomerEntity> children = repo.findByLinkedId(curr.getId());
            for (CustomerEntity child : children) {
                if (!result.contains(child)) {
                    result.add(child);
                    queue.add(child);
                }
            }
        }
        return result;
    }

    // Demote a customer tree (and all its descendants) to secondary, point to new root
    private void demoteTreeToSecondary(CustomerEntity loser, Long newRootId) {
        List<CustomerEntity> toUpdate = collectAllLinked(loser);
        for (CustomerEntity c : toUpdate) {
            if ("primary".equals(c.getLinkedPreference())) {
                c.setLinkedPreference("secondary");
                c.setLinkedId(newRootId);
                repo.save(c);
            } else if (c.getLinkedId() != null && !c.getLinkedId().equals(newRootId)) {
                c.setLinkedId(newRootId);
                repo.save(c);
            }
        }
    }

    // Compose API response per your DTO spec, always put primary's email/phone first in lists
    private CustomerResponseDTO formatResponse(CustomerEntity primary, List<CustomerEntity> cluster) {
        List<String> emails = cluster.stream()
                .map(CustomerEntity::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> phones = cluster.stream()
                .map(CustomerEntity::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Ensure primary's email/phone are first in respective lists
        if (primary.getEmail() != null && emails.contains(primary.getEmail())) {
            emails.remove(primary.getEmail());
            emails.add(0, primary.getEmail());
        }
        if (primary.getPhoneNumber() != null && phones.contains(primary.getPhoneNumber())) {
            phones.remove(primary.getPhoneNumber());
            phones.add(0, primary.getPhoneNumber());
        }

        List<Long> secondaryIds = cluster.stream()
                .filter(c -> "secondary".equals(c.getLinkedPreference()))
                .map(CustomerEntity::getId)
                .collect(Collectors.toList());

        CustomerResponseDTO.Contact contactDTO = new CustomerResponseDTO.Contact(
                primary.getId(),
                emails,
                phones,
                secondaryIds
        );
        return new CustomerResponseDTO(contactDTO);
    }
}