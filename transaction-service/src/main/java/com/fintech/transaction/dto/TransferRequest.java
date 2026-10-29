package com.fintech.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferRequest(@NotNull Long sourceAccountId, @NotNull Long targetAccountId,
                              @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                              @Pattern(regexp = "[A-Za-z]{2}") String country,
                              @Size(max = 500) String description) {
}