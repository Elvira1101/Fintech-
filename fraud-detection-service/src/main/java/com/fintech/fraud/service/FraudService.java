package com.fintech.fraud.service;

import com.fintech.fraud.dto.FraudAlertResponse;
import com.fintech.fraud.dto.FraudCheckRequest;
import com.fintech.fraud.dto.FraudCheckResponse;
import com.fintech.fraud.entity.FraudCheck;
import com.fintech.fraud.entity.FraudStatus;
import com.fintech.fraud.repository.FraudCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FraudService {
    private final FraudCheckRepository repository;
    private final List<FraudRule> rules;

    public FraudService(FraudCheckRepository repository, List<FraudRule> rules) {
        this.repository = repository;
        this.rules = rules;
    }

    @Transactional
    public FraudCheckResponse evaluate(FraudCheckRequest request) {
        Instant now = Instant.now();
        long recentChecks = "TRANSFER".equals(request.transactionType())
                ? repository.countRecentChecks(request.sourceAccountId(), now.minus(10, ChronoUnit.MINUTES)) : 0;
        FraudContext context = new FraudContext(request.sourceAccountId(), request.amount(), request.transactionType(),
                request.country().toUpperCase(), recentChecks);
        List<FraudRule.FraudFinding> findings = rules.stream().map(rule -> rule.evaluate(context))
                .flatMap(java.util.Optional::stream).toList();
        FraudStatus status = findings.stream().anyMatch(f -> f.status() == FraudStatus.BLOCKED) ? FraudStatus.BLOCKED
                : findings.isEmpty() ? FraudStatus.ACCEPTED : FraudStatus.SUSPICIOUS;
        String reason = findings.isEmpty() ? "Aucune règle de fraude déclenchée"
                : findings.stream().map(FraudRule.FraudFinding::reason).distinct().collect(java.util.stream.Collectors.joining("; "));

        FraudCheck check = new FraudCheck();
        check.setAccountId(request.sourceAccountId());
        check.setTransactionType(request.transactionType());
        check.setAmount(request.amount());
        check.setCountry(context.country());
        check.setStatus(status);
        check.setReason(reason);
        check.setCheckedAt(now);
        repository.save(check);
        return new FraudCheckResponse(status, reason);
    }

    @Transactional(readOnly = true)
    public List<FraudAlertResponse> alerts() {
        return repository.findByStatusNotOrderByCheckedAtDesc(FraudStatus.ACCEPTED).stream()
                .map(check -> new FraudAlertResponse(check.getId(), check.getAccountId(), check.getAmount(),
                        check.getCountry(), check.getStatus(), check.getReason(), check.getCheckedAt())).toList();
    }
}