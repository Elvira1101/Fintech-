package com.fintech.account.controller;

import com.fintech.account.dto.AccountRequest;
import com.fintech.account.dto.AccountResponse;
import com.fintech.account.dto.BalanceMutationRequest;
import com.fintech.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public AccountResponse create(@Valid @RequestBody AccountRequest request, Authentication auth) {
        return service.create(request, auth);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public List<AccountResponse> list(Authentication auth) {
        return service.list(auth);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public AccountResponse get(@PathVariable Long id, Authentication auth) {
        return service.get(id, auth);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public AccountResponse update(@PathVariable Long id, @Valid @RequestBody AccountRequest request, Authentication auth) {
        return service.update(id, request, auth);
    }

    @GetMapping("/internal/{id}")
    public AccountResponse getInternal(@PathVariable Long id, @RequestHeader("X-Internal-Key") String key) {
        return service.getInternal(id, key);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth);
    }

    @PostMapping("/internal/{id}/credit")
    public AccountResponse credit(@PathVariable Long id, @Valid @RequestBody BalanceMutationRequest request,
                                  @RequestHeader("X-Internal-Key") String key) {
        return service.creditInternal(id, request, key);
    }

    @PostMapping("/internal/{id}/debit")
    public AccountResponse debit(@PathVariable Long id, @Valid @RequestBody BalanceMutationRequest request,
                                 @RequestHeader("X-Internal-Key") String key) {
        return service.debitInternal(id, request, key);
    }

    @PostMapping("/internal/{id}/compensate-credit")
    public AccountResponse compensateCredit(@PathVariable Long id, @Valid @RequestBody BalanceMutationRequest request,
                                            @RequestHeader("X-Internal-Key") String key) {
        return service.compensateCredit(id, request, key);
    }

    @PostMapping("/internal/{id}/compensate-debit")
    public AccountResponse compensateDebit(@PathVariable Long id, @Valid @RequestBody BalanceMutationRequest request,
                                           @RequestHeader("X-Internal-Key") String key) {
        return service.compensateDebit(id, request, key);
    }
}