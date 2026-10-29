package com.fintech.transaction.client;

import com.fintech.transaction.dto.AccountSnapshot;
import com.fintech.transaction.dto.BalanceMutationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "account-service")
public interface AccountClient {
    @GetMapping("/api/accounts/internal/{id}")
    AccountSnapshot get(@PathVariable("id") Long id, @RequestHeader("X-Internal-Key") String key);

    @PostMapping("/api/accounts/internal/{id}/debit")
    AccountSnapshot debit(@PathVariable("id") Long id, @RequestBody BalanceMutationRequest request,
                          @RequestHeader("X-Internal-Key") String key);

    @PostMapping("/api/accounts/internal/{id}/credit")
    AccountSnapshot credit(@PathVariable("id") Long id, @RequestBody BalanceMutationRequest request,
                           @RequestHeader("X-Internal-Key") String key);

    @PostMapping("/api/accounts/internal/{id}/compensate-debit")
    AccountSnapshot compensateDebit(@PathVariable("id") Long id, @RequestBody BalanceMutationRequest request,
                                    @RequestHeader("X-Internal-Key") String key);

    @PostMapping("/api/accounts/internal/{id}/compensate-credit")
    AccountSnapshot compensateCredit(@PathVariable("id") Long id, @RequestBody BalanceMutationRequest request,
                                     @RequestHeader("X-Internal-Key") String key);
}