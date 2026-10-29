package com.fintech.fraud.service;

import com.fintech.fraud.entity.FraudStatus;
import java.util.Optional;

public interface FraudRule {
    Optional<FraudFinding> evaluate(FraudContext context);

    record FraudFinding(FraudStatus status, String reason) {
    }
}