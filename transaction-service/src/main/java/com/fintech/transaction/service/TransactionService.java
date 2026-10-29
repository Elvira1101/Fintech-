package com.fintech.transaction.service;

import com.fintech.transaction.client.AccountClient;
import com.fintech.transaction.client.FraudClient;
import com.fintech.transaction.dto.AccountSnapshot;
import com.fintech.transaction.dto.BalanceMutationRequest;
import com.fintech.transaction.dto.DepositRequest;
import com.fintech.transaction.dto.FraudCheckRequest;
import com.fintech.transaction.dto.FraudCheckResponse;
import com.fintech.transaction.dto.TransactionResponse;
import com.fintech.transaction.dto.TransferRequest;
import com.fintech.transaction.entity.BankTransaction;
import com.fintech.transaction.entity.TransactionStatus;
import com.fintech.transaction.entity.TransactionType;
import com.fintech.transaction.mapper.TransactionMapper;
import com.fintech.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final FraudClient fraudClient;
    private final String internalKey;

    public TransactionService(TransactionRepository repository, AccountClient accountClient, FraudClient fraudClient,
                              @Value("${security.internal-key}") String internalKey) {
        this.repository = repository;
        this.accountClient = accountClient;
        this.fraudClient = fraudClient;
        this.internalKey = internalKey;
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, Authentication auth) {
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new IllegalArgumentException("Les comptes source et cible doivent être différents");
        }
        AccountSnapshot source = accountClient.get(request.sourceAccountId(), internalKey);
        AccountSnapshot target = accountClient.get(request.targetAccountId(), internalKey);
        requireOwner(source, auth);
        FraudCheckResponse decision = fraudClient.check(new FraudCheckRequest(source.id(), request.amount(),
                TransactionType.TRANSFER.name(), normalizeCountry(request.country())), internalKey);
        if (decision.status() == TransactionStatus.BLOCKED) {
            return TransactionMapper.toResponse(save(TransactionType.TRANSFER, decision.status(), source.id(), target.id(),
                    request.amount(), normalizeCountry(request.country()), auth.getName(), decision.reason()));
        }

        BalanceMutationRequest mutation = new BalanceMutationRequest(request.amount(), request.description());
        accountClient.debit(source.id(), mutation, internalKey);
        try {
            accountClient.credit(target.id(), mutation, internalKey);
        } catch (RuntimeException exception) {
            // Les bases étant distinctes, annuler le débit déjà validé côté account-service.
            compensate(() -> accountClient.compensateDebit(source.id(), mutation, internalKey), exception);
            throw exception;
        }
        try {
            return TransactionMapper.toResponse(save(TransactionType.TRANSFER, decision.status(), source.id(), target.id(),
                    request.amount(), normalizeCountry(request.country()), auth.getName(), decision.reason()));
        } catch (RuntimeException exception) {
            compensate(() -> accountClient.compensateCredit(target.id(), mutation, internalKey), exception);
            compensate(() -> accountClient.compensateDebit(source.id(), mutation, internalKey), exception);
            throw exception;
        }
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request, Authentication auth) {
        AccountSnapshot target = accountClient.get(request.targetAccountId(), internalKey);
        requireOwner(target, auth);
        FraudCheckResponse decision = fraudClient.check(new FraudCheckRequest(target.id(), request.amount(),
                TransactionType.DEPOSIT.name(), normalizeCountry(request.country())), internalKey);
        if (decision.status() == TransactionStatus.BLOCKED) {
            return TransactionMapper.toResponse(save(TransactionType.DEPOSIT, decision.status(), null, target.id(),
                    request.amount(), normalizeCountry(request.country()), auth.getName(), decision.reason()));
        }
        BalanceMutationRequest mutation = new BalanceMutationRequest(request.amount(), request.description());
        accountClient.credit(target.id(), mutation, internalKey);
        try {
            return TransactionMapper.toResponse(save(TransactionType.DEPOSIT, decision.status(), null, target.id(),
                    request.amount(), normalizeCountry(request.country()), auth.getName(), decision.reason()));
        } catch (RuntimeException exception) {
            compensate(() -> accountClient.compensateCredit(target.id(), mutation, internalKey), exception);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<BankTransaction> transactions = admin ? repository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).toList()
                : repository.findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(auth.getName());
        return transactions.stream().map(TransactionMapper::toResponse).toList();
    }

    private BankTransaction save(TransactionType type, TransactionStatus status, Long sourceId, Long targetId,
                                 java.math.BigDecimal amount, String country, String ownerEmail, String reason) {
        BankTransaction transaction = new BankTransaction();
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setSourceAccountId(sourceId);
        transaction.setTargetAccountId(targetId);
        transaction.setAmount(amount);
        transaction.setCountry(country);
        transaction.setOwnerEmail(ownerEmail);
        transaction.setFraudReason(reason);
        transaction.setCreatedAt(Instant.now());
        return repository.save(transaction);
    }

    private void requireOwner(AccountSnapshot account, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !account.ownerEmail().equalsIgnoreCase(auth.getName())) {
            throw new SecurityException("Accès interdit à ce compte");
        }
    }

    private String normalizeCountry(String country) {
        return country == null || country.isBlank() ? "FR" : country.toUpperCase();
    }

    private void compensate(Runnable action, RuntimeException original) {
        try {
            action.run();
        } catch (RuntimeException compensationFailure) {
            original.addSuppressed(compensationFailure);
        }
    }
}