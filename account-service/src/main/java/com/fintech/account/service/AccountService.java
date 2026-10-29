package com.fintech.account.service;

import com.fintech.account.dto.AccountRequest;
import com.fintech.account.dto.AccountResponse;
import com.fintech.account.dto.BalanceMutationRequest;
import com.fintech.account.entity.Account;
import com.fintech.account.mapper.AccountMapper;
import com.fintech.account.repository.AccountRepository;
import com.fintech.common.security.InternalRequestValidator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final InternalRequestValidator internalValidator;
    private final String internalKey;

    public AccountService(AccountRepository repository, InternalRequestValidator internalValidator,
                          @Value("${security.internal-key}") String internalKey) {
        this.repository = repository;
        this.internalValidator = internalValidator;
        this.internalKey = internalKey;
    }

    @Transactional
    public AccountResponse create(AccountRequest request, Authentication auth) {
        Account account = new Account();
        account.setAccountNumber("FR" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        account.setOwnerEmail(auth.getName());
        account.setType(request.type());
        account.setBalance(BigDecimal.ZERO);
        return AccountMapper.toResponse(repository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return (admin ? repository.findAll() : repository.findByOwnerEmailIgnoreCase(auth.getName()))
                .stream().map(AccountMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id, Authentication auth) {
        return AccountMapper.toResponse(findOwned(id, auth));
    }

    @Transactional
    public AccountResponse update(Long id, AccountRequest request, Authentication auth) {
        Account account = findOwned(id, auth);
        if (account.getBalance().signum() != 0) {
            throw new IllegalStateException("Le type d'un compte approvisionné ne peut pas être modifié");
        }
        account.setType(request.type());
        return AccountMapper.toResponse(repository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getInternal(Long id, String suppliedKey) {
        internalValidator.requireValid(suppliedKey);
        Account account = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Compte introuvable"));
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Account account = findOwned(id, auth);
        if (account.getBalance().signum() != 0) {
            throw new IllegalStateException("Un compte dont le solde n'est pas nul ne peut pas être supprimé");
        }
        repository.delete(account);
    }

    @Transactional
    public AccountResponse creditInternal(Long id, BalanceMutationRequest request, String suppliedKey) {
        internalValidator.requireValid(suppliedKey);
        Account account = findForUpdate(id);
        account.setBalance(account.getBalance().add(request.amount()));
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse debitInternal(Long id, BalanceMutationRequest request, String suppliedKey) {
        internalValidator.requireValid(suppliedKey);
        Account account = findForUpdate(id);
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Solde insuffisant");
        }
        account.setBalance(account.getBalance().subtract(request.amount()));
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse compensateCredit(Long id, BalanceMutationRequest request, String suppliedKey) {
        internalValidator.requireValid(suppliedKey);
        Account account = findForUpdate(id);
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Impossible de compenser le crédit : solde déjà consommé");
        }
        account.setBalance(account.getBalance().subtract(request.amount()));
        return AccountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse compensateDebit(Long id, BalanceMutationRequest request, String suppliedKey) {
        internalValidator.requireValid(suppliedKey);
        Account account = findForUpdate(id);
        account.setBalance(account.getBalance().add(request.amount()));
        return AccountMapper.toResponse(account);
    }

    private Account findOwned(Long id, Authentication auth) {
        Account account = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Compte introuvable"));
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !account.getOwnerEmail().equalsIgnoreCase(auth.getName())) {
            throw new SecurityException("Accès interdit à ce compte");
        }
        return account;
    }

    private Account findForUpdate(Long id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> new EntityNotFoundException("Compte introuvable"));
    }
}