package com.fintech.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalRequestValidator {
    private final String expectedKey;

    public InternalRequestValidator(@Value("${security.internal-key}") String expectedKey) {
        this.expectedKey = expectedKey;
    }

    public void requireValid(String suppliedKey) {
        if (suppliedKey == null || !MessageDigest.isEqual(expectedKey.getBytes(StandardCharsets.UTF_8),
                suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new SecurityException("Clé interservices invalide");
        }
    }
}