package com.fintech.fraud.service;

import com.fintech.fraud.entity.FraudStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudRulesTest {
    private final AmountThresholdRule amountRule = new AmountThresholdRule();
    private final VelocityRule velocityRule = new VelocityRule();
    private final UnusualCountryRule countryRule = new UnusualCountryRule("FR");

    @Test
    void amountAboveTenThousandIsBlocked() {
        var result = amountRule.evaluate(new FraudContext(1L, new BigDecimal("10000.01"), "TRANSFER", "FR", 0));

        assertEquals(FraudStatus.BLOCKED, result.orElseThrow().status());
    }

    @Test
    void unusualCountryIsSuspicious() {
        var result = countryRule.evaluate(new FraudContext(1L, new BigDecimal("20.00"), "TRANSFER", "US", 0));

        assertEquals(FraudStatus.SUSPICIOUS, result.orElseThrow().status());
    }

    @Test
    void sixthTransferWithinWindowIsSuspicious() {
        var result = velocityRule.evaluate(new FraudContext(1L, new BigDecimal("20.00"), "TRANSFER", "FR", 5));

        assertEquals(FraudStatus.SUSPICIOUS, result.orElseThrow().status());
    }

    @Test
    void fiveOrFewerTransfersDoNotTriggerVelocityRule() {
        var result = velocityRule.evaluate(new FraudContext(1L, new BigDecimal("20.00"), "TRANSFER", "FR", 4));

        assertTrue(result.isEmpty());
    }

    @Test
    void largeDepositIsNotBlockedByTransferThreshold() {
        var result = amountRule.evaluate(new FraudContext(1L, new BigDecimal("15000.00"), "DEPOSIT", "FR", 0));

        assertTrue(result.isEmpty());
    }
}