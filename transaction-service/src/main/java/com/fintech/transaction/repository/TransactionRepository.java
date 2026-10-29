package com.fintech.transaction.repository;

import com.fintech.transaction.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(String ownerEmail);
}