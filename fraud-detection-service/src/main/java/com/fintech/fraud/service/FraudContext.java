package com.fintech.fraud.service;

import java.math.BigDecimal;

public record FraudContext(Long sourceAccountId, BigDecimal amount, String transactionType, String country, long recentChecks) {
}