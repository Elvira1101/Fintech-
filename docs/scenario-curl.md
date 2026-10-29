# Scénario curl de bout en bout

Démarrer Eureka puis auth, customer, account, fraud, transaction et enfin gateway. Nécessite `curl` et `jq`. Fournir les variables requises aux processus comme indiqué dans le README, puis définir `OTP_CODE` dans ce terminal avec la même valeur que celle d’auth-service.

```bash
BASE=http://localhost:8080
EMAIL=client@example.test
PASSWORD=$(openssl rand -base64 18)

# 1. Inscription
curl -sS -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" | jq

# 2. Vérification du mot de passe ; utiliser la valeur OTP_CODE fournie au processus auth-service
curl -sS -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" | jq

# 3. Vérification OTP et récupération du JWT
TOKEN=$(curl -sS -X POST "$BASE/api/auth/verify-otp" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"code\":\"$OTP_CODE\"}" | jq -r '.accessToken')

# 4. Création de deux comptes
SOURCE=$(curl -sS -X POST "$BASE/api/accounts" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"type":"CURRENT"}')
SOURCE_ID=$(printf '%s' "$SOURCE" | jq -r '.id')
TARGET=$(curl -sS -X POST "$BASE/api/accounts" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"type":"SAVINGS"}')
TARGET_ID=$(printf '%s' "$TARGET" | jq -r '.id')

# Approvisionner le compte source par deux dépôts autorisés
curl -sS -X POST "$BASE/api/transactions/deposit" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d "{\"targetAccountId\":$SOURCE_ID,\"amount\":6000,\"country\":\"FR\"}" | jq
curl -sS -X POST "$BASE/api/transactions/deposit" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d "{\"targetAccountId\":$SOURCE_ID,\"amount\":6000,\"country\":\"FR\"}" | jq

# 5. Virement normal : ACCEPTED
curl -sS -X POST "$BASE/api/transactions/transfer" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"sourceAccountId\":$SOURCE_ID,\"targetAccountId\":$TARGET_ID,\"amount\":125.50,\"country\":\"FR\",\"description\":\"Virement normal\"}" | jq

# 6. Virement > 10 000 EUR : enregistré BLOCKED, aucun solde n'est modifié
curl -sS -X POST "$BASE/api/transactions/transfer" -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"sourceAccountId\":$SOURCE_ID,\"targetAccountId\":$TARGET_ID,\"amount\":10000.01,\"country\":\"FR\"}" | jq

# Consulter soldes et historique
curl -sS "$BASE/api/accounts" -H "Authorization: Bearer $TOKEN" | jq
curl -sS "$BASE/api/transactions" -H "Authorization: Bearer $TOKEN" | jq
```

Pour examiner les alertes, un JWT portant le rôle `RISK_ANALYST` est requis sur `GET /api/fraud/alerts`. Le rôle ne peut pas être choisi à l’inscription publique ; le provisionnement des comptes analyste doit être réalisé par un mécanisme d’administration contrôlé.