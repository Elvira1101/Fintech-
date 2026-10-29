package com.fintech.transaction.dto;

import com.fintech.transaction.entity.TransactionStatus;
import com.fintech.transaction.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(Long id, TransactionType type, TransactionStatus status,
                                 Long sourceAccountId, Long targetAccountId, BigDecimal amount,
                                 String country, String fraudReason, Instant createdAt) {
}