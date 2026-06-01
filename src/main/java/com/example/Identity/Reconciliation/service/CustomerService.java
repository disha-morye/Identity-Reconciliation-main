package com.example.Identity.Reconciliation.service;


import com.example.Identity.Reconciliation.DTO.CustomerRequestDTO;
import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.entity.CustomerEntity;
import com.example.Identity.Reconciliation.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService{

    @Autowired
    public CustomerRepository customerRepository;

    public CustomerResponseDTO addc(CustomerRequestDTO customerRequestDTO){
        String email = customerRequestDTO.getEmail();
        String phoneNumber = customerRequestDTO.getPhoneNumber();

        if (email == null && phoneNumber == null) {
            throw new IllegalArgumentException("Either email or phoneNumber must be provided");
        }

        List<CustomerEntity> matchedContacts = customerRepository.findByPhoneNumberOrEmail(phoneNumber, email);

        if (matchedContacts.isEmpty()) {

            CustomerEntity newContact = new CustomerEntity();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phoneNumber);
            newContact.setLinkedPreference("primary");

            CustomerEntity saved = customerRepository.save(newContact);

            CustomerResponseDTO response = new CustomerResponseDTO();
            CustomerResponseDTO.Contact contact = new CustomerResponseDTO.Contact();
            contact.setPrimaryContatctId(saved.getId());
            contact.setEmails(email != null ? List.of(email) : new ArrayList<>());
            contact.setPhoneNumbers(phoneNumber != null ? List.of(phoneNumber) : new ArrayList<>());
            contact.setSecondaryContactIds(new ArrayList<>());

            response.setContact(contact);
            return response;
        }

        // Will handle this logic in the next step (linking logic)
        return null;
    }

}