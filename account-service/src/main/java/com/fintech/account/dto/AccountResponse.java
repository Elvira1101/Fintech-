package com.fintech.account.dto;

import com.fintech.account.entity.AccountType;
import java.math.BigDecimal;

public record AccountResponse(Long id, String accountNumber, String ownerEmail, AccountType type,
                              BigDecimal balance, long version) {
}