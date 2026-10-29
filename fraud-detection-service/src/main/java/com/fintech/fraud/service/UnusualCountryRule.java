package com.fintech.fraud.service;

import com.fintech.fraud.entity.FraudStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

@Component
public class UnusualCountryRule implements FraudRule {
    private final Set<String> allowedCountries;

    public UnusualCountryRule(@Value("${fraud.allowed-countries:FR}") String countries) {
        this.allowedCountries = Arrays.stream(countries.split(",")).map(String::trim)
                .map(String::toUpperCase).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<FraudFinding> evaluate(FraudContext context) {
        return allowedCountries.contains(context.country().toUpperCase()) ? Optional.empty()
                : Optional.of(new FraudFinding(FraudStatus.SUSPICIOUS, "Pays inhabituel : " + context.country().toUpperCase()));
    }
}