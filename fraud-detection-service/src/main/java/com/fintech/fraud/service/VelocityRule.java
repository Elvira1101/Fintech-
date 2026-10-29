package com.fintech.fraud.service;

import com.fintech.fraud.entity.FraudStatus;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class VelocityRule implements FraudRule {
    @Override
    public Optional<FraudFinding> evaluate(FraudContext context) {
        return context.recentChecks() >= 5
                ? Optional.of(new FraudFinding(FraudStatus.SUSPICIOUS, "Plus de 5 virements en 10 minutes"))
                : Optional.empty();
    }
}