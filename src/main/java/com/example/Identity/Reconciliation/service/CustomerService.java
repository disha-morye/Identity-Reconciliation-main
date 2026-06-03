package com.example.Identity.Reconciliation.service;

import com.example.Identity.Reconciliation.DTO.CustomerRequestDTO;
import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.entity.CustomerEntity;
import com.example.Identity.Reconciliation.repository.CustomerRepository;
import com.example.Identity.Reconciliation.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    // Constructor injection of repository.
    @Autowired
    public CustomerService(@Lazy CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public ResponseEntity<CustomerResponseDTO> identifyCustomerContact(CustomerRequestDTO customerRequestDTO) {

        String email = customerRequestDTO.getEmail();
        String phoneNumber = customerRequestDTO.getPhoneNumber();

        if (email == null && phoneNumber == null) {
            throw new IllegalArgumentException("Both email and phoneNumber cannot be null");
        }

        // Finding matching record with request params
        Optional<CustomerEntity> emailMatch = email != null ? customerRepository.findFirstByEmail(email) : Optional.empty();
        Optional<CustomerEntity> phoneMatch = phoneNumber != null ? customerRepository.findFirstByPhoneNumber(phoneNumber) : Optional.empty();

        // If it is new customer then insert into database.
        if (emailMatch.isEmpty() && phoneMatch.isEmpty()) {
            CustomerEntity primaryCustomerRecord = new CustomerEntity();
            primaryCustomerRecord.setEmail(email);
            primaryCustomerRecord.setPhoneNumber(phoneNumber);
            primaryCustomerRecord.setLinkedPreference("primary");
            customerRepository.save(primaryCustomerRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(Utils.formatResponse(primaryCustomerRecord, Collections.emptyList()));
        }

        CustomerEntity primaryEntity = null;
        // If all request params are present in database then find the primary customer of the params
        if (emailMatch.isPresent() && phoneMatch.isPresent()) {
            // Could be different clusters, resolve each to primary, for potential merge
            CustomerEntity primaryCustomerFromEmail = Utils.findPrimaryCustomer(emailMatch.get(), customerRepository);
            CustomerEntity primaryCustomerFromPhone = Utils.findPrimaryCustomer(phoneMatch.get(), customerRepository);

            if (!primaryCustomerFromEmail.getId().equals(primaryCustomerFromPhone.getId())) {
                // Merging two trees, oldest as primary
                CustomerEntity olderCustomerEntity = primaryCustomerFromEmail.getCreatedAt().isBefore(primaryCustomerFromPhone.getCreatedAt()) ? primaryCustomerFromEmail : primaryCustomerFromPhone;
                CustomerEntity recentCustomerEntity = olderCustomerEntity == primaryCustomerFromEmail ? primaryCustomerFromPhone : primaryCustomerFromEmail;
                Utils.demoteTreeToSecondary(recentCustomerEntity, olderCustomerEntity.getId(), customerRepository);
                primaryEntity = olderCustomerEntity;
            } else {
                primaryEntity = primaryCustomerFromEmail;
            }
        } else {
            // Only one side match, needs creation of new entry in database.
            primaryEntity = emailMatch.isPresent() ? Utils.findPrimaryCustomer(emailMatch.get(), customerRepository) : Utils.findPrimaryCustomer(phoneMatch.get(), customerRepository);
        }

        // Check if exact match exists (`both fields match the same customer`)
        Optional<CustomerEntity> existedCustomerEntity = customerRepository.findFirstByPhoneNumberAndEmail(phoneNumber, email);

        if (existedCustomerEntity.isPresent()) {
            List<CustomerEntity> allCluster = Utils.collectAllSecondaryCustomers(primaryEntity, customerRepository);
            return ResponseEntity.status(HttpStatus.OK).body(Utils.formatResponse(primaryEntity, allCluster));
        }

        //Only one field matches (partial match), add as secondary
        if (emailMatch.isEmpty() || phoneMatch.isEmpty()) {
            if ((email != null && phoneNumber != null) || (email == null && phoneMatch.isEmpty()) || (phoneNumber == null && emailMatch.isEmpty())) {
                CustomerEntity secondaryEntity = new CustomerEntity();
                secondaryEntity.setEmail(email);
                secondaryEntity.setPhoneNumber(phoneNumber);
                secondaryEntity.setLinkedPreference("secondary");
                secondaryEntity.setLinkedId(primaryEntity.getId());
                customerRepository.save(secondaryEntity);
            }

            List<CustomerEntity> secondaryCustomers = Utils.collectAllSecondaryCustomers(primaryEntity, customerRepository);
            return ResponseEntity.status(HttpStatus.CREATED).body(Utils.formatResponse(primaryEntity, secondaryCustomers));
        }

        List<CustomerEntity> allCluster = Utils.collectAllSecondaryCustomers(primaryEntity, customerRepository);
        return ResponseEntity.ok(Utils.formatResponse(primaryEntity, allCluster));
    }
}