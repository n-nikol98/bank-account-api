# Bank Account API

A Spring Boot 4 / Java 21 banking demo application for managing accounts, deposits and transfers.

## Run with Docker

From this directory:

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`.

To override the default environment variables for the app and PostgreSQL container, pass them inline when you run Compose:

```bash
DB_NAME=bank_demo DB_USERNAME=bank_user DB_PASSWORD=secret DB_PORT=5433 APP_PORT=8081 docker compose up --build
```

Alternatively, you can put the same values in a separate `.env` file and let Compose pick them up automatically.

To reset the local PostgreSQL volume and recreate the database from scratch:

```bash
docker compose down -v
```

## Run Bruno E2E tests

Start the API first, then open the `e2e-tests` directory as a Bruno collection
and select the `Local` environment. The collection expects the API at
`http://localhost:8080/api`.

With the Bruno CLI installed:

```bash
bru run e2e-tests --env Local
```

The requests are ordered so the account IDs created by the first folder are
available to later requests. The collection uses only test-created accounts,
so it can be run repeatedly against a local database.

## Build and test locally

Prerequisites: Java 21+ and Maven 3.9+.

```bash
mvn test
mvn package
```

## API examples

```bash
FROM=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H 'content-type: application/json' -d '{"initialBalance":100.00,"currency":"EUR"}' | jq -r .id)
TO=$(curl -s -X POST http://localhost:8080/api/accounts \
  -H 'content-type: application/json' -d '{"currency":"EUR"}' | jq -r .id)
curl -X POST "http://localhost:8080/api/accounts/$TO/deposit" \
  -H 'content-type: application/json' -d '{"amount":25.00}'
curl -X POST "http://localhost:8080/api/accounts/$FROM/transfer/$TO" \
  -H 'content-type: application/json' \
  -d '{"amount":30.00}'
```

## Persistence and concurrency choices

PostgreSQL runs in Docker with a persistent volume, and JPA's schema update mode ensures that the existing database structure and data are preserved when the application restarts, unless the volume is explicitly deleted.

Supported currencies are business configurations stored in the `currencies` table and seeded with `EUR`, `USD`, and `GBP`.

Money is stored as `DECIMAL(19,2)`, never floating point.

Deposits and transfers run in database transactions. Transfers acquire pessimistic write locks for both accounts in deterministic `UUID` order, preventing lost updates and avoiding deadlocks when concurrent transfers operate in opposite directions.

Transfers between different currencies are not supported; exchange-rate conversion is outside of this demo's scope.
