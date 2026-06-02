package com.example.Identity.Reconciliation.service;

import com.example.Identity.Reconciliation.DTO.CustomerRequestDTO;
import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.entity.CustomerEntity;
import com.example.Identity.Reconciliation.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repo;

    @Transactional
    public ResponseEntity<CustomerResponseDTO> identifyCustomerContact(CustomerRequestDTO customerRequestDTO) {

        String email = customerRequestDTO.getEmail();
        String phoneNumber = customerRequestDTO.getPhoneNumber();
        if(email == null &&  phoneNumber == null){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        Optional<CustomerEntity> emailMatch = email != null ?repo.findFirstByEmail(email): Optional.empty()  ;
        Optional<CustomerEntity> phoneMatch = phoneNumber != null ? repo.findFirstByPhoneNumber(phoneNumber) : Optional.empty() ;

        System.out.println(emailMatch);
        System.out.println(phoneMatch);
        // --- CASE 1: New unique identity (no match for email/phone)
        if (emailMatch.isEmpty() && phoneMatch.isEmpty()) {
            CustomerEntity primaryCustomerRecord = new CustomerEntity();
            primaryCustomerRecord.setEmail(email);
            primaryCustomerRecord.setPhoneNumber(phoneNumber);
            primaryCustomerRecord.setLinkedPreference("primary");
            repo.save(primaryCustomerRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(formatResponse(primaryCustomerRecord, Collections.emptyList()));
        }

        // Find all the roots for the email and phone matches (if any)
        CustomerEntity primaryEntity = null;
        if (emailMatch.isPresent() && phoneMatch.isPresent()) {
            // Could be different clusters, resolve each to root, for potential merge
            CustomerEntity primaryCustomerFromEmail = findPrimaryCustomer(emailMatch.get());
            CustomerEntity primaryCustomerFromPhone = findPrimaryCustomer(phoneMatch.get());

            if (!primaryCustomerFromEmail.getId().equals(primaryCustomerFromPhone.getId())) {
                // Merge two trees, oldest as primary
                CustomerEntity olderCustomerEntity = primaryCustomerFromEmail.getCreatedAt().isBefore(primaryCustomerFromPhone.getCreatedAt()) ? primaryCustomerFromEmail : primaryCustomerFromPhone;
                CustomerEntity recentCustomerEntity = olderCustomerEntity == primaryCustomerFromEmail ? primaryCustomerFromPhone : primaryCustomerFromEmail;
                demoteTreeToSecondary(recentCustomerEntity, olderCustomerEntity.getId());
                primaryEntity = olderCustomerEntity;
            } else {
                primaryEntity = primaryCustomerFromEmail;
            }
        } else {
            // Only one side matches, climb up to the root
            primaryEntity = emailMatch.isPresent() ? findPrimaryCustomer(emailMatch.get()) : findPrimaryCustomer(phoneMatch.get());
        }

        // Check if exact match exists (`both fields match the same customer`)
        Optional<CustomerEntity> existedCustomerEntity= repo.findFirstByPhoneNumberAndEmail(phoneNumber,email);

        if (existedCustomerEntity.isPresent()) {
            List<CustomerEntity> allCluster = collectAllSecondaryCustomers(primaryEntity);
            return ResponseEntity.status(HttpStatus.OK).body(formatResponse(primaryEntity, allCluster));
        }

        // --- CASE 2: Only one field matches (partial match), add as secondary
        if (emailMatch.isEmpty()  ||  phoneMatch.isEmpty()) {
            if((email != null && phoneNumber != null) || (email == null && phoneMatch.isEmpty()) || (phoneNumber==null && emailMatch.isEmpty())){
                CustomerEntity secondaryEntity = new CustomerEntity();
                secondaryEntity.setEmail(email);
                secondaryEntity.setPhoneNumber(phoneNumber);
                secondaryEntity.setLinkedPreference("secondary");
                secondaryEntity.setLinkedId(primaryEntity.getId());
                repo.save(secondaryEntity);
            }

            List<CustomerEntity> secondaryCustomers = collectAllSecondaryCustomers(primaryEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(formatResponse(primaryEntity, secondaryCustomers));
        }

        // --- CASE 4: Both fields match but different clusters, handled above (merge)... now just return merged cluster
        List<CustomerEntity> allCluster = collectAllSecondaryCustomers(primaryEntity);
        return ResponseEntity.ok(formatResponse(primaryEntity, allCluster));
    }

    private CustomerEntity findPrimaryCustomer(CustomerEntity customer) {
        return customer.getLinkedId() != null ? repo.findById(customer.getLinkedId()).orElse(customer) : customer;
    }

    // Downward traversal: get all customers in this cluster (BFS)
    private List<CustomerEntity> collectAllSecondaryCustomers(CustomerEntity root) {
        List<CustomerEntity> result = new ArrayList<>();
        result.add(root);
        result.addAll(repo.findByLinkedId(root.getId()));
        return result;
    }

    // Demote a customer tree (and all its descendants) to secondary, point to new root
    private void demoteTreeToSecondary(CustomerEntity recentCustomerEntity, Long newRootId) {
        List<CustomerEntity> toUpdate = collectAllSecondaryCustomers(recentCustomerEntity);
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
        List<String> emails = new ArrayList<>();
        List<String> phones = new ArrayList<>();

        if (!cluster.isEmpty()) {
            emails = cluster.stream().map(CustomerEntity::getEmail).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            phones = cluster.stream().map(CustomerEntity::getPhoneNumber).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }


        System.out.println(emails + "emails ");
        System.out.println(phones + "emails ");
        // Ensure primary's email/phone are first in respective lists
        if (!emails.isEmpty() && primary.getEmail() != null && emails.contains(primary.getEmail())) {
            emails.remove(primary.getEmail());
            emails.add(0, primary.getEmail());
        }
        if (!phones.isEmpty() && primary.getPhoneNumber() != null && phones.contains(primary.getPhoneNumber())) {
            phones.remove(primary.getPhoneNumber());
            phones.add(0, primary.getPhoneNumber());
        }


        List<Long> secondaryIds = new ArrayList<>();
        if (!cluster.isEmpty()) {
            secondaryIds = cluster.stream().filter(c -> "secondary".equals(c.getLinkedPreference())).map(CustomerEntity::getId).collect(Collectors.toList());
        }

        CustomerResponseDTO.Contact contactDTO = new CustomerResponseDTO.Contact(
                primary.getId(),
                emails,
                phones,
                secondaryIds
        );
        return new CustomerResponseDTO(contactDTO);
    }
}