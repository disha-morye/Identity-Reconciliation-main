package com.example.Identity.Reconciliation.repository;

import com.example.Identity.Reconciliation.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long>{

    Optional<CustomerEntity> findFirstByEmail(String email);

    Optional<CustomerEntity> findFirstByPhoneNumber(String phoneNumber);

    Optional<CustomerEntity> findFirstByPhoneNumberAndEmail(String phoneNumber, String email);

    List<CustomerEntity> findByLinkedId(Long linkedId);

}