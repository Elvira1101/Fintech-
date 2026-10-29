package com.fintech.fraud.dto;

import com.fintech.fraud.entity.FraudStatus;

public record FraudCheckResponse(FraudStatus status, String reason) {
}