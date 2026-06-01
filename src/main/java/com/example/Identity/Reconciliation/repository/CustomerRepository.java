package com.example.Identity.Reconciliation.repository;

import com.example.Identity.Reconciliation.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long>{
    List<CustomerEntity> findByPhoneNumberOrEmail(String phoneNumber, String email);
}
