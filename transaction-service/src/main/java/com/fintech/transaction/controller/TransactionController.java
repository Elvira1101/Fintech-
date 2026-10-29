package com.fintech.transaction.controller;

import com.fintech.transaction.dto.DepositRequest;
import com.fintech.transaction.dto.TransactionResponse;
import com.fintech.transaction.dto.TransferRequest;
import com.fintech.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request, Authentication auth) {
        return service.transfer(request, auth);
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@Valid @RequestBody DepositRequest request, Authentication auth) {
        return service.deposit(request, auth);
    }

    @GetMapping
    public List<TransactionResponse> history(Authentication auth) {
        return service.history(auth);
    }
}