package com.fintech.transaction.mapper;

import com.fintech.transaction.dto.TransactionResponse;
import com.fintech.transaction.entity.BankTransaction;

public final class TransactionMapper {
    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(BankTransaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getType(), transaction.getStatus(),
                transaction.getSourceAccountId(), transaction.getTargetAccountId(), transaction.getAmount(),
                transaction.getCountry(), transaction.getFraudReason(), transaction.getCreatedAt());
    }
}