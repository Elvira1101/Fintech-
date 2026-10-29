# Fintech distribué

Plateforme de démonstration bancaire composée de services Spring Boot 3 / Java 17, découverte Eureka, Gateway réactive, JWT, H2 isolé par service et détection de fraude.

## Modules

`discovery-service` (8761), `gateway-service` (8080), `auth-service` (8081), `customer-service` (8082), `account-service` (8083), `transaction-service` (8084), `fraud-detection-service` (8085), ainsi que `security-common`.

## Démarrage

Prérequis : JDK 17+ et Maven 3.9+. Lancer chaque commande dans un terminal séparé, dans cet ordre (les processus restent au premier plan) :

```bash
mvn clean package
mvn -pl discovery-service spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl customer-service spring-boot:run
mvn -pl account-service spring-boot:run
mvn -pl fraud-detection-service spring-boot:run
mvn -pl transaction-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

Les bases H2 sont indépendantes et en mémoire. Les consoles locales sont disponibles sur `/h2-console` (authentification désactivée uniquement pour ce chemin de démonstration). Les Swagger UI sont agrégées par la Gateway à `http://localhost:8080/swagger-ui.html`; Eureka est à `http://localhost:8761`.

Les secrets ne sont pas stockés dans le dépôt. Avant de lancer les services concernés, fournir `JWT_SECRET` (au moins 32 octets) à auth, customer, account, transaction, fraud-detection et gateway ; `INTERNAL_SERVICE_KEY` à customer, account, transaction et fraud-detection ; et `OTP_CODE` à auth. L’application ne démarre pas si une valeur requise manque. Les fournir via l’environnement ou un gestionnaire de secrets, jamais dans les fichiers suivis par Git. L’OTP configuré est une valeur de démonstration fixe ; aucun service d’envoi d’OTP n’est fourni.

Une inscription publique reçoit toujours le rôle `CLIENT`. Pour créer un analyste au démarrage d’auth-service, définir `BOOTSTRAP_ANALYST_EMAIL` et `BOOTSTRAP_ANALYST_PASSWORD` (12 caractères minimum) ; le compte `ADMIN` peut être créé de la même façon avec `BOOTSTRAP_ADMIN_EMAIL` et `BOOTSTRAP_ADMIN_PASSWORD`. Ces comptes utilisent aussi le code défini par `OTP_CODE`.

## Scénario complet

Voir [`docs/scenario-curl.md`](docs/scenario-curl.md) pour l’inscription, l’OTP, la création de comptes, le virement accepté et le virement supérieur à 10 000 € bloqué.

Le MCD Mermaid et le MLD SQL sont dans [`docs/data-model.md`](docs/data-model.md) et [`docs/schema.sql`](docs/schema.sql).

## Limite transactionnelle

Chaque service possède sa propre base. `@Transactional` garantit l’atomicité locale ; le virement distribué utilise une compensation (saga) si l’écriture du journal échoue après modification des comptes. Une atomicité ACID stricte entre les bases nécessiterait une transaction distribuée, non fournie par Eureka/Feign.