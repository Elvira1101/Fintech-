package com.fintech.fraud.service;

import com.fintech.fraud.entity.FraudStatus;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Optional;

@Component
public class AmountThresholdRule implements FraudRule {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");

    @Override
    public Optional<FraudFinding> evaluate(FraudContext context) {
        return "TRANSFER".equals(context.transactionType()) && context.amount().compareTo(MAX_AMOUNT) > 0
                ? Optional.of(new FraudFinding(FraudStatus.BLOCKED, "Montant supérieur à 10 000 EUR"))
                : Optional.empty();
    }
}