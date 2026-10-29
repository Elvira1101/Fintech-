package com.fintech.account.dto;

import com.fintech.account.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public record AccountRequest(@NotNull AccountType type) {
}