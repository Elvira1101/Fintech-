package com.fintech.account.mapper;

import com.fintech.account.dto.AccountResponse;
import com.fintech.account.entity.Account;

public final class AccountMapper {
    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getAccountNumber(), account.getOwnerEmail(),
                account.getType(), account.getBalance(), account.getVersion() == null ? 0 : account.getVersion());
    }
}