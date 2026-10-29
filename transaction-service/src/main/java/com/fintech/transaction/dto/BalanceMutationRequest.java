package com.fintech.transaction.dto;

import java.math.BigDecimal;

public record BalanceMutationRequest(BigDecimal amount, String reference) {
}