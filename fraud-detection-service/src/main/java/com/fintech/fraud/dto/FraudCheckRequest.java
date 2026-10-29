package com.fintech.fraud.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record FraudCheckRequest(@NotNull Long sourceAccountId,
                                @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                                @NotBlank @Pattern(regexp = "TRANSFER|DEPOSIT") String transactionType,
                                @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String country) {
}