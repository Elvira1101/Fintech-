package com.fintech.transaction.dto;

import java.math.BigDecimal;

public record FraudCheckRequest(Long sourceAccountId, BigDecimal amount, String transactionType, String country) {
}