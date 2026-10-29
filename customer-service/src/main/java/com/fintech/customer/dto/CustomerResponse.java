package com.fintech.customer.dto;

public record CustomerResponse(Long id, String email, String firstName, String lastName, String phone) {
}