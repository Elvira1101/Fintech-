package com.fintech.fraud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fraud_checks")
@Getter
@Setter
@NoArgsConstructor
public class FraudCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long accountId;
    @Column(nullable = false)
    private String transactionType;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 2)
    private String country;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudStatus status;
    @Column(nullable = false)
    private String reason;
    @Column(nullable = false)
    private Instant checkedAt;
}