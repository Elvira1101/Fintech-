# Modèle de données

## MCD

```mermaid
erDiagram
    USER }o--o{ ROLE : possede
    CUSTOMER ||--o{ ACCOUNT : detient
    ACCOUNT ||--o{ BANK_TRANSACTION : source
    ACCOUNT ||--o{ BANK_TRANSACTION : destination
    ACCOUNT ||--o{ FRAUD_CHECK : controle

    USER {
        bigint id PK
        string email UK
        string password_hash
    }
    ROLE {
        bigint id PK
        string name UK
    }
    CUSTOMER {
        bigint id PK
        string email
        string first_name
        string last_name
        string phone
    }
    ACCOUNT {
        bigint id PK
        string account_number UK
        string owner_email
        string type
        decimal balance
        bigint version
    }
    BANK_TRANSACTION {
        bigint id PK
        string type
        string status
        bigint source_account_id FK
        bigint target_account_id FK
        decimal amount
        string country
        string owner_email
        datetime created_at
    }
    FRAUD_CHECK {
        bigint id PK
        bigint account_id FK
        string transaction_type
        decimal amount
        string country
        string status
        datetime checked_at
    }
```

Les liens entre services sont des références logiques : les bases H2 restent isolées et ne portent pas de contraintes FK interservices.

## MLD SQL (account-service et transaction-service)

Le script exécutable est [`schema.sql`](schema.sql). Les entités JPA créent ces tables automatiquement dans les bases H2 séparées.