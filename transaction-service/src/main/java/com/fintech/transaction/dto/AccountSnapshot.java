package com.fintech.transaction.dto;

import java.math.BigDecimal;

public record AccountSnapshot(Long id, String accountNumber, String ownerEmail, String type,
                              BigDecimal balance, long version) {
}