
# Iniciar Aplicativo

```bash
lein run
```
> Aviso! Este software foi escrito por uma pessoa que não tem proficiência nesta linguagem e foi escrito em menos de 2 horas, então releve.

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
curl -H "x-user-id: USER_ID" http://localhost:80800/v1/account
```

## Endpoint: GET /v1/movements

- Recupera as movimentações da conta do usuário dentro de um período específico.

  ### Headers:

  - x-user-id (obrigatório): ID do usuário.

  Parâmetros de consulta:
  - start (obrigatório): Data de início no formato "yyyy-MM-dd".
  - end (obrigatório): Data de término no formato "yyyy-MM-dd".
  Exemplo de chamada cURL:

  ```bash
  curl -H "x-user-id: USER_ID" "http://localhost:80800/v1/movements?start=2023-01-01&end=2023-12-31"
  ```

- Recupera as movimentações da conta do usuário de um determinado tipo.

  ```bash
  curl -H "x-user-id: USER_ID" "http://localhost:80800/v1/movements?type=CREDIT"
  ```

- Recupera as movimentações da conta do usuário de um determinado tipo e dentro de um período específico.
  ```bash
  curl -H "x-user-id: USER_ID" "http://localhost:80800/v1/movements?type=CREDIT&start=2023-01-01&end=2023-12-31"
  ```

Certifique-se de ajustar a URL http://localhost:8080 de acordo com o endereço do servidor onde o aplicativo está sendo executado.