package com.fintech.transaction.dto;

import com.fintech.transaction.entity.TransactionStatus;

public record FraudCheckResponse(TransactionStatus status, String reason) {
}