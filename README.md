
# Iniciar Aplicativo

```bash
lein run
```

# API Wallet

## Endpoint: POST /v1/transfer

Realiza uma transferência de fundos entre contas.

### Headers:

- x-user-id (obrigatório): ID do usuário.
- Content-Type: application/json
Corpo da solicitação:

```json
{
  "account": "ACCOUNT_ID",
  "value": 100.0,
  "description": "Descrição da transferência"
}
```
Exemplo de chamada cURL:

```bash
curl -X POST -H "x-user-id: USER_ID" -H "Content-Type: application/json" -d '{"account": "ACCOUNT_ID", "value": 100.0, "description": "Descrição da transferência"}' http://localhost:8080/v1/transfer
```

## Endpoint: GET /v1/account

Recupera as informações da conta do usuário.

### Headers:

- x-user-id (obrigatório): ID do usuário.

Exemplo de chamada cURL:

```bash
curl -H "x-user-id: USER_ID" http://localhost:3000/v1/account
```

## Endpoint: GET /v1/movements/period

Recupera as movimentações da conta do usuário dentro de um período específico.

### Headers:

- x-user-id (obrigatório): ID do usuário.

Parâmetros de consulta:
- start (obrigatório): Data de início no formato "yyyy-MM-dd".
- end (obrigatório): Data de término no formato "yyyy-MM-dd".
Exemplo de chamada cURL:

```bash
curl -H "x-user-id: USER_ID" "http://localhost:3000/v1/movements/period?start=2023-01-01&end=2023-12-31"
```

## Endpoint: GET /v1/movements/type

Recupera as movimentações da conta do usuário de um determinado tipo.

### Headers:

- x-user-id (obrigatório): ID do usuário.

Parâmetros de consulta:
- type (obrigatório): Tipo de movimentação.
Exemplo de chamada cURL:

```bash
curl -H "x-user-id: USER_ID" "http://localhost:3000/v1/movements/type?type=CREDIT"
```

Certifique-se de ajustar a URL http://localhost:8080 de acordo com o endereço do servidor onde o aplicativo está sendo executado.