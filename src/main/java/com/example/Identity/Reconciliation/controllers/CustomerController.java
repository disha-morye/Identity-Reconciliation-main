package com.example.Identity.Reconciliation.controllers;

import com.example.Identity.Reconciliation.DTO.CustomerRequestDTO;
import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/api")
public class CustomerController {

    private final CustomerService customerService;

    // Constructor injection of service.
    @Autowired
    public CustomerController(@Lazy CustomerService customerService){
        this.customerService = customerService;
    }

    @PostMapping(path = "/identify")
    @Async
    public CompletableFuture<ResponseEntity<?>> getCustomerContact(@RequestBody CustomerRequestDTO customerRequestDTO){
        return CompletableFuture.completedFuture(customerService.identifyCustomerContact(customerRequestDTO));
    }
}