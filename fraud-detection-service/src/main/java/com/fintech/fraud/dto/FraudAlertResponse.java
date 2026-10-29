package com.fintech.fraud.dto;

import com.fintech.fraud.entity.FraudStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record FraudAlertResponse(Long id, Long accountId, BigDecimal amount, String country,
                                 FraudStatus status, String reason, Instant checkedAt) {
}