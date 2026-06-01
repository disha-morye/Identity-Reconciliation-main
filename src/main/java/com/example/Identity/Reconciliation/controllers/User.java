package com.example.Identity.Reconciliation.controllers;

import com.example.Identity.Reconciliation.DTO.CustomerRequestDTO;
import com.example.Identity.Reconciliation.DTO.CustomerResponseDTO;
import com.example.Identity.Reconciliation.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api")
public class User{

    @Autowired
    public CustomerService customerService;

    public User(){
        System.out.println("user bean created");
    }

    @PostMapping(path = "/user")
    public CustomerResponseDTO func(@RequestBody CustomerRequestDTO customerRequestDTO){
        return customerService.addc(customerRequestDTO);
    }
}