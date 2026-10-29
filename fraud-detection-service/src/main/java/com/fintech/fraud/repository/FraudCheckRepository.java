package com.fintech.fraud.repository;

import com.fintech.fraud.entity.FraudCheck;
import com.fintech.fraud.entity.FraudStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, Long> {
    long countByAccountIdAndCheckedAtAfter(Long accountId, Instant checkedAt);
    List<FraudCheck> findByStatusNotOrderByCheckedAtDesc(FraudStatus status);
    @Query("select count(f) from FraudCheck f where f.accountId = :accountId and f.transactionType = 'TRANSFER' and f.checkedAt > :since")
    long countRecentChecks(@Param("accountId") Long accountId, @Param("since") Instant since);
}