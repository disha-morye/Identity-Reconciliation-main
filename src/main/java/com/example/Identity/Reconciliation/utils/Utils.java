package com.example.Identity.Reconciliation.utils;


import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.entity.CustomerEntity;
import com.example.Identity.Reconciliation.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

    private final static String PRIMARY = "primary";
    private final static String SECONDARY = "secondary";

    // Util function for building response.
    public static CustomerResponseDTO formatResponse(CustomerEntity primary, List<CustomerEntity> cluster) {
        List<String> emails = new ArrayList<>();
        List<String> phones = new ArrayList<>();

        // Collecting only distinct secondary email and phone numbers.
        if (!cluster.isEmpty()) {
            emails = cluster.stream().map(CustomerEntity::getEmail).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            phones = cluster.stream().map(CustomerEntity::getPhoneNumber).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }

        // Ensuring primary's email/phone are first in respective lists
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
            secondaryIds = cluster.stream().filter(c -> SECONDARY.equals(c.getLinkedPreference())).map(CustomerEntity::getId).collect(Collectors.toList());
        }

        CustomerResponseDTO.Contact contactDTO = new CustomerResponseDTO.Contact(primary.getId(), emails, phones, secondaryIds);
        return new CustomerResponseDTO(contactDTO);
    }

    // Demote a customer tree and all its descendants to secondary if
    // it is part of same customer whose primary was created earlier.
    public static void demoteTreeToSecondary(CustomerEntity recentCustomerEntity, Long newRootId, CustomerRepository customerRepository) {
        List<CustomerEntity> toUpdate = collectAllSecondaryCustomers(recentCustomerEntity, customerRepository);
        for (CustomerEntity c : toUpdate) {
            if (PRIMARY.equals(c.getLinkedPreference())) {
                c.setLinkedPreference(SECONDARY);
                c.setLinkedId(newRootId);
                customerRepository.save(c);
            } else if (c.getLinkedId() != null && !c.getLinkedId().equals(newRootId)) {
                c.setLinkedId(newRootId);
                customerRepository.save(c);
            }
        }
    }

    // Util function for getting all secondary connection of a primary customer.
    public static List<CustomerEntity> collectAllSecondaryCustomers(CustomerEntity root, CustomerRepository customerRepository) {
        List<CustomerEntity> result = new ArrayList<>();
        result.add(root);
        result.addAll(customerRepository.findByLinkedId(root.getId()));
        return result;
    }

    public static CustomerEntity findPrimaryCustomer(CustomerEntity customer, CustomerRepository customerRepository) {
        return customer.getLinkedId() != null ? customerRepository.findById(customer.getLinkedId()).orElse(customer) : customer;
    }
}