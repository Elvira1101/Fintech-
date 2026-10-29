package com.fintech.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(@NotBlank @Email @Size(max = 160) String email,
                              @NotBlank @Size(max = 80) String firstName,
                              @NotBlank @Size(max = 80) String lastName,
                              @NotBlank @Pattern(regexp = "[+0-9() .-]{7,24}") String phone) {
}