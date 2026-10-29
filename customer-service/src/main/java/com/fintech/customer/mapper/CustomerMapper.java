package com.fintech.customer.mapper;

import com.fintech.customer.dto.CustomerRequest;
import com.fintech.customer.dto.CustomerResponse;
import com.fintech.customer.entity.Customer;

public final class CustomerMapper {
    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setEmail(request.email().trim().toLowerCase());
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone().trim());
        return customer;
    }

    public static void update(Customer customer, CustomerRequest request) {
        customer.setEmail(request.email().trim().toLowerCase());
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone().trim());
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getEmail(), customer.getFirstName(),
                customer.getLastName(), customer.getPhone());
    }
}