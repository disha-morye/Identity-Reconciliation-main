package com.example.Identity.Reconciliation.repository;

import com.example.Identity.Reconciliation.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity,Long>{
//    @Query("SELECT c FROM CustomerEntity c " +
//            "WHERE (:email IS NOT NULL AND c.email = :email) " +
//            "   OR (:phone IS NOT NULL AND c.phoneNumber = :phone)")
//    List<CustomerEntity> getMatchingContacts(@Param("email") String email, @Param("phone") String phoneNumber);

    List<CustomerEntity> findByEmail(String email);

    List<CustomerEntity> findByPhoneNumber(String phoneNumber);

    List<CustomerEntity> findByLinkedId(Long linkedId);
}