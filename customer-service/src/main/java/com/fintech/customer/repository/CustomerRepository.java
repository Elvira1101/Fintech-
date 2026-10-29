package com.fintech.customer.repository;

import com.fintech.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByEmailIgnoreCase(String email);
}