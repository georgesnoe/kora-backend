# Kora Backend

REST API for **Kora**, a multi-tenant retail / enterprise management platform. This service owns
identity and access management: user registration, email verification, credential sign-in, JWT
session management, roles, permissions, enterprises, and stores.

The application is a stateless Spring Boot service. PostgreSQL is the system of record; Flyway owns
the schema. Access and refresh tokens are signed HS256 JWTs, and refresh tokens are persisted as
SHA-256 hashes so that sessions can be revoked server-side.

- **Group / artifact:** `com.kora:backend`
- **Runtime:** Java 25
- **Framework:** Spring Boot 4.1.2 (Spring MVC, Spring Security, Spring Data JPA)
- **Database:** PostgreSQL 17 + Flyway
- **Mail:** SMTP (Mailpit locally) + Thymeleaf HTML templates
- **Build:** Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Container:** Alpine JRE on a `scratch` (distroless) final stage
- **Repository:** <https://github.com/georgesnoe/kora-backend>

---

## Table of contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Project structure](#project-structure)
4. [Prerequisites](#prerequisites)
5. [Quick start](#quick-start)
6. [Configuration](#configuration)
7. [Build, run, and test](#build-run-and-test)
8. [API conventions](#api-conventions)
9. [API reference](#api-reference)
10. [Authentication and token model](#authentication-and-token-model)
11. [Email verification flow](#email-verification-flow)
12. [Database schema](#database-schema)
13. [Error reference](#error-reference)
14. [Docker](#docker)
15. [Continuous integration and delivery](#continuous-integration-and-delivery)
16. [Security notes](#security-notes)
17. [Troubleshooting](#troubleshooting)

---

## Features

- Email/password registration with BCrypt password hashing.
- Email ownership verification using a numeric, single-use, expiring code.
- Sign-in, sign-out, and refresh-token rotation.
- Stateless request authentication via signed JWT access tokens.
- Server-side session revocation: every token is bound to a persisted session (`sid` claim).
- Role-based and permission-based domain model (roles, permissions, `superAdminOnly` flags).
- Enterprise and store entities with many-to-many user membership.
- Consistent JSON error contract for validation, security, and unexpected failures.
- Flyway migrations on startup, Hibernate schema validation (`ddl-auto: validate`).
- Actuator health endpoint for container orchestration.
- Distroless Alpine container image with no shell and no package manager.

---

## Architecture

```text
Client
  │  HTTPS / JSON
  ▼
Spring Security filter chain
  │  JwtAuthenticationFilter  ──► validates access JWT + checks session row
  ▼
AuthController  (/api/auth/**)
  ▼
AuthService ──► UserRepository / AccountRepository / SessionRepository
  │        └──► VerificationService ──► VerificationRepository
  │        └──► JwtService (HS256 encode/decode)
  │        └──► EmailService ──► SMTP + Thymeleaf template
  ▼
PostgreSQL (Flyway-managed schema)
```

Key design points:

- The API is **stateless**. No HTTP session is created (`SessionCreationPolicy.STATELESS`), CSRF is
  disabled, and Spring's form login / HTTP Basic / default logout are disabled.
- An access token is accepted only if it is a valid, unexpired JWT **and** its `sid` still exists in
  the `sessions` table. Deleting the session immediately invalidates its access token.
- Refresh tokens are rotated on every refresh. Only the SHA-256 hash of the current refresh token is
  stored in the database.
- Emails are normalized to lowercase, trimmed, before storage and lookup.
- Validation failures, domain errors, and security errors all return the same `ErrorResponse` shape.

---

## Project structure

```text
backend/
├── .github/workflows/ci.yml        # CI: test, Docker build, smoke test, GHCR publish
├── .mvn/                           # Maven wrapper
├── docker-compose.yaml             # Local PostgreSQL + Mailpit
├── Dockerfile                      # Multi-stage distroless Alpine image
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/kora/backend/
    │   │   ├── BackendApplication.java
    │   │   ├── config/             # Jwt/Mail/Verification properties, SecurityConfig, OpenApiConfig
    │   │   ├── controller/         # AuthController
    │   │   ├── dto/
    │   │   │   ├── request/        # Sign-up, sign-in, refresh, sign-out, verify, resend
    │   │   │   └── response/       # Auth, user, sign-up, message, error responses
    │   │   ├── entity/             # User, Account, Session, Verification, Role, Permission, ...
    │   │   ├── exception/          # ApiException hierarchy + GlobalExceptionHandler
    │   │   ├── repository/         # Spring Data JPA repositories
    │   │   ├── security/           # JWT filter, auth entry point, access denied handler
    │   │   ├── service/            # AuthService, JwtService, VerificationService, EmailService
    │   │   └── util/               # HashingUtils
    │   └── resources/
    │       ├── application.yaml
    │       ├── db/migration/       # V1, V2, V3 Flyway migrations
    │       └── templates/email/    # verify-account.html
    └── test/java/com/kora/backend/ # Spring context test + OpenAPI generation test
```

---

## Prerequisites

| Tool | Version |
| --- | --- |
| JDK | 25 |
| Docker | 24+ (with BuildKit) |
| Docker Compose | v2 |
| Maven | Not required; use the wrapper |

`docker-compose.yaml` starts:

- **PostgreSQL 17** on `localhost:5432` (`postgres` / `postgres`, database `postgres`)
- **Mailpit** SMTP on `localhost:1025` and web UI/API on <http://localhost:8025>

---

## Quick start

```bash
# 1. Start PostgreSQL and Mailpit
docker compose up -d

# 2. Run the application (defaults match docker-compose)
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# 3. Health check
curl http://localhost:8080/actuator/health

# 4. Interactive API documentation
# http://localhost:8080/swagger-ui.html
```

The API is available at <http://localhost:8080/api>. Emails sent during registration are captured
by Mailpit at <http://localhost:8025>. The generated OpenAPI description is served at
<http://localhost:8080/v3/api-docs.yaml>.

Typical first flow:

1. `POST /api/auth/sign-up`
2. Open the verification email in Mailpit and copy the 6-digit code.
3. `POST /api/auth/verify` with `identifier` and `code`.
4. `POST /api/auth/sign-in` to receive an access token and refresh token.

---

## Configuration

All settings can be provided as environment variables. Defaults are suitable for local development
only.

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/postgres` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `MAIL_HOST` | `localhost` | SMTP host |
| `MAIL_PORT` | `1025` | SMTP port |
| `MAIL_USERNAME` | *(empty)* | SMTP username |
| `MAIL_PASSWORD` | *(empty)* | SMTP password |
| `MAIL_SMTP_AUTH` | `false` | Enable SMTP authentication |
| `MAIL_SMTP_STARTTLS` | `false` | Enable STARTTLS |
| `MAIL_FROM` | `no-reply@kora.local` | From address |
| `MAIL_FROM_NAME` | `Kora` | From display name |
| `JWT_SECRET` | development secret | HS256 secret, **must be at least 32 bytes** |
| `JWT_ISSUER` | `kora` | `iss` claim and validator issuer |
| `JWT_ACCESS_TOKEN_TTL` | `PT1H` | Access token lifetime (ISO-8601 duration) |
| `JWT_REFRESH_TOKEN_TTL` | `P7D` | Refresh token lifetime (ISO-8601 duration) |
| `VERIFICATION_CODE_LENGTH` | `6` | Verification code length (minimum 4) |
| `VERIFICATION_TTL` | `PT24H` | Verification code lifetime |
| `VERIFICATION_URL` | `http://localhost:8080/api/auth/verify` | Base URL embedded in verification emails |
| `SERVER_PORT` | `8080` | HTTP port (standard Spring Boot variable) |
| `SPRINGDOC_API_DOCS_ENABLED` | `true` | Expose `/v3/api-docs` and `/v3/api-docs.yaml` |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | `true` | Expose the Swagger UI at `/swagger-ui.html` |

> **Production:** provide a random `JWT_SECRET` of at least 32 bytes, use TLS for SMTP, and point
> `VERIFICATION_URL` at the public API host.

---

## Build, run, and test

```bash
# Compile and package (skips tests)
./mvnw -B -ntp -DskipTests package

# Compile and run all tests (requires PostgreSQL)
./mvnw -B -ntp test

# Full verification
./mvnw -B -ntp verify

# Run the packaged jar
java -jar target/backend-0.0.1-SNAPSHOT.jar

# Run with an explicit profile/port and environment
DB_URL=jdbc:postgresql://localhost:5432/postgres \
JWT_SECRET='replace-with-a-random-32-byte-or-longer-secret' \
java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=8080
```

On Windows PowerShell:

```powershell
.\mvnw.cmd -B -ntp verify
java -jar target\backend-0.0.1-SNAPSHOT.jar
```

The context test (`BackendApplicationTests`) boots the full Spring context, connects to PostgreSQL,
and applies Flyway migrations. Start `docker compose up -d` before running tests locally.

---

## API conventions

| Item | Value |
| --- | --- |
| Base URL (local) | `http://localhost:8080` |
| API prefix | `/api` |
| Content type | `application/json` |
| Authentication | `Authorization: Bearer <accessToken>` |
| Timestamps in errors | ISO-8601 UTC (`Instant`) |
| IDs | UUID strings |
| Token type | Always `Bearer` |

- Request bodies must be valid JSON. A missing or malformed body returns `400 INVALID_REQUEST_BODY`.
- Unknown routes return `404 RESOURCE_NOT_FOUND`; wrong HTTP methods return `405 METHOD_NOT_ALLOWED`.
- Unsupported `Content-Type` returns `415 UNSUPPORTED_MEDIA_TYPE`.
- Protected endpoints without a valid token return `401 UNAUTHORIZED`.
- Field validation returns `400 VALIDATION_FAILED` with a `fieldErrors` object.

### Error response shape

Every error is normalized to:

```json
{
  "timestamp": "2026-11-07T21:52:46.178536400Z",
  "status": 401,
  "code": "UNAUTHORIZED",
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource",
  "path": "/api/private",
  "fieldErrors": {}
}
```

Validation example:

```json
{
  "timestamp": "2026-11-07T21:53:12.004Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/auth/sign-up",
  "fieldErrors": {
    "name": "Name must be between 2 and 100 characters",
    "email": "Email must be a valid email address",
    "password": "Password must contain at least one letter and one digit"
  }
}
```

`fieldErrors` maps a field name to the first validation message for that field. It is `{}` when the
error is not a field-validation error.

---

## API reference

### Endpoint summary

| Method | Path | Auth | Success | Purpose |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/sign-up` | Public | `201` | Create an account and send a verification email |
| `POST` | `/api/auth/sign-in` | Public | `200` | Authenticate and create a session |
| `POST` | `/api/auth/refresh` | Bearer access token | `200` | Rotate the refresh token and issue a new access token |
| `POST` | `/api/auth/sign-out` | Bearer access token | `200` | Revoke a session |
| `POST` | `/api/auth/verify` | Public | `200` | Verify an email address with a JSON body |
| `GET` | `/api/auth/verify` | Public | `200` | Verify an email address from a link |
| `POST` | `/api/auth/resend-verification` | Bearer access token | `200` | Re-send a verification email |
| `GET` | `/actuator/health` | Public | `200` | Service health |

Public routes are `/api/auth/sign-up`, `/api/auth/sign-in`, and both `/api/auth/verify` variants.
`/api/auth/refresh`, `/api/auth/sign-out`, and `/api/auth/resend-verification` require a valid
access token; the caller's email-verification status is not checked by the security layer. Any other
route requires a valid access token.

---

### 1. Create an account

```http
POST /api/auth/sign-up
Content-Type: application/json
```

Request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "S3cretPass"
}
```

Validation rules:

| Field | Rules |
| --- | --- |
| `name` | Required, not blank, 2–100 characters; surrounding whitespace is trimmed |
| `email` | Required, valid email, at most 255 characters; stored lowercase |
| `password` | Required, 8–72 characters, contains at least one letter and one digit |

Response `201 Created`:

```json
{
  "message": "Account created. Please check your inbox to verify your email address.",
  "user": {
    "id": "61d252f5-d9b2-4760-aee5-972e90083712",
    "name": "Ada Lovelace",
    "email": "ada@example.com",
    "emailVerified": false,
    "image": null,
    "roles": []
  }
}
```

Errors:

| Status | Code | Cause |
| --- | --- | --- |
| `400` | `VALIDATION_FAILED` | One or more fields invalid |
| `409` | `EMAIL_ALREADY_EXISTS` | An account with the normalized email already exists |
| `500` | `INTERNAL_SERVER_ERROR` | Unexpected server failure |

A verification code is generated and a verification email is sent. If SMTP delivery fails, the
failure is logged and the account is still created (`201`); the code remains usable until resend.

`curl`:

```bash
curl -i -X POST http://localhost:8080/api/auth/sign-up \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","password":"S3cretPass"}'
```

---

### 2. Sign in

```http
POST /api/auth/sign-in
Content-Type: application/json
```

Request:

```json
{
  "email": "ada@example.com",
  "password": "S3cretPass"
}
```

Optional request headers recorded on the session:

- `X-Forwarded-For` (first address is stored as the session IP)
- `User-Agent`

Response `200 OK`:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "61d252f5-d9b2-4760-aee5-972e90083712",
    "name": "Ada Lovelace",
    "email": "ada@example.com",
    "emailVerified": true,
    "image": null,
    "roles": []
  }
}
```

`expiresIn` is the access-token lifetime in **seconds**.

Errors:

| Status | Code | Cause |
| --- | --- | --- |
| `400` | `VALIDATION_FAILED` | Missing/invalid email or password |
| `401` | `INVALID_CREDENTIALS` | Unknown email or wrong password |
| `403` | `EMAIL_NOT_VERIFIED` | Correct credentials, but the email is not verified |

---

### 3. Refresh a session

```http
POST /api/auth/refresh
Content-Type: application/json
Authorization: Bearer <accessToken>
```

The caller must be authenticated with a valid access token. The access token's session must still
exist. If the access token has expired, the endpoint returns `401` and the client must sign in
again; a refresh token by itself is not accepted.

Request:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Response `200 OK`: same shape as the sign-in response. The supplied refresh token is rotated; the
old refresh token can no longer be used.

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "61d252f5-d9b2-4760-aee5-972e90083712",
    "name": "Ada Lovelace",
    "email": "ada@example.com",
    "emailVerified": true,
    "image": null,
    "roles": []
  }
}
```

Errors:

| Status | Code | Cause |
| --- | --- | --- |
| `400` | `VALIDATION_FAILED` | Missing/blank `refreshToken` |
| `401` | `UNAUTHORIZED` | Missing or invalid access token |
| `401` | `INVALID_TOKEN` | Token malformed, expired, wrong type, revoked, or session mismatch |

---

### 4. Sign out

```http
POST /api/auth/sign-out
Content-Type: application/json
Authorization: Bearer <accessToken>
```

Authentication is required; a missing or invalid access token returns `401 UNAUTHORIZED`.

Request body (optional):

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Behavior:

1. If a non-blank `refreshToken` is supplied, the matching session is deleted (if it exists).
2. Otherwise, the session identified by the access token's `sid` claim is deleted.

Response `200 OK`:

```json
{
  "message": "Signed out successfully."
}
```

The operation is idempotent: signing out with an unknown or already-revoked refresh token still
returns `200`. After sign-out, the refresh token is unusable and the associated access token no
longer authenticates because its session no longer exists.

Errors:

| Status | Code | Cause |
| --- | --- | --- |
| `401` | `UNAUTHORIZED` | Missing or invalid access token |

---

### 5. Verify an email address (JSON)

```http
POST /api/auth/verify
Content-Type: application/json
```

Request:

```json
{
  "identifier": "ada@example.com",
  "code": "123456"
}
```

Response `200 OK`:

```json
{
  "message": "Your email address has been verified. You can now sign in."
}
```

Errors:

| Status | Code | Cause |
| --- | --- | --- |
| `400` | `VALIDATION_FAILED` | Missing identifier or code, or invalid email format |
| `400` | `VERIFICATION_FAILED` | Code invalid, expired, or no pending verification |

---

### 6. Verify an email address (link)

```http
GET /api/auth/verify?identifier=ada@example.com&code=123456
```

This is the endpoint used by verification emails. Query parameters are URL-encoded. Response and
errors are the same as the JSON variant.

| Status | Code | Cause |
| --- | --- | --- |
| `200` | — | Email verified |
| `400` | `MISSING_PARAMETER` | `identifier` or `code` missing |
| `400` | `VERIFICATION_FAILED` | Code invalid or expired |

`curl`:

```bash
curl "http://localhost:8080/api/auth/verify?identifier=ada%40example.com&code=123456"
```

---

### 7. Resend a verification email

```http
POST /api/auth/resend-verification
Content-Type: application/json
Authorization: Bearer <accessToken>
```

Authentication is required; a missing or invalid access token returns `401 UNAUTHORIZED`. The
security layer does not check whether the caller's email has been verified.

Request:

```json
{
  "email": "ada@example.com"
}
```

Response `200 OK` (always the same message, to avoid account enumeration):

```json
{
  "message": "If an account with that email exists and is not verified, a new verification email has been sent."
}
```

Behavior:

- If the account exists and is unverified, a new code replaces the previous one and an email is sent.
- If the account does not exist or is already verified, nothing is sent.
- The response is identical in all cases.

> **Note:** sign-in currently rejects unverified accounts with `403 EMAIL_NOT_VERIFIED`. Since this
> endpoint now requires an access token, an unverified user cannot call it until the sign-in policy
> is relaxed or a separate unauthenticated resend flow is added.

Errors:

| Status | Code | Cause |
| --- | --- | --- |
| `400` | `VALIDATION_FAILED` | Missing/invalid email |
| `401` | `UNAUTHORIZED` | Missing or invalid access token |

---

### 8. Health

```http
GET /actuator/health
```

Response `200 OK`:

```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

Health probes are available at `/actuator/health/liveness` and `/actuator/health/readiness`. The
endpoint and its subpaths are public; every other Actuator endpoint is not exposed.

---

## OpenAPI documentation

OpenAPI 3.1 documentation is generated by `springdoc-openapi` from annotations on the controllers
and DTOs.

| Resource | Location |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |
| Build artifact | `target/openapi.yaml` |

All documentation endpoints are public (explicitly permitted in `SecurityConfig`).

### Build-time generation

`OpenApiDocumentationTest` boots the application on a random port, downloads
`/v3/api-docs.yaml`, verifies that the expected auth paths and the `bearerAuth` scheme are present,
and writes the result to `target/openapi.yaml`. The test runs during the Maven `test` phase, so:

```bash
./mvnw verify
# target/openapi.yaml now contains the complete specification
```

CI uploads this file as the `openapi-spec` artifact. The specification includes every route,
request/response schema, documented status code, and the `bearerAuth` requirement for protected
operations.

### Annotations used

- `@OpenAPIDefinition` — API title, version, description, contact, tags, and servers
- `@SecurityScheme(name = "bearerAuth")` — HTTP bearer JWT security scheme
- `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse(s)` — controller documentation
- `@Schema` — DTO field descriptions, examples, constraints, and write-only secrets

To disable the documentation in a hardened environment:

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

---

## Authentication and token model

### Tokens

| Property | Access token | Refresh token |
| --- | --- | --- |
| Purpose | Authorize API requests | Obtain new token pairs |
| Default lifetime | `PT1H` | `P7D` |
| Transport | `Authorization: Bearer ...` | JSON request body only |
| Server-side storage | None (only the session row matters) | SHA-256 hex hash in `sessions.token` |
| Rotation | New access token on each refresh | Rotated on each refresh |

### JWT format

- Algorithm: **HS256** (shared secret).
- Header: `{"alg":"HS256"}`.
- Claims:

| Claim | Meaning |
| --- | --- |
| `iss` | Configured issuer (`kora` by default) |
| `sub` | User ID |
| `iat` | Issued-at time |
| `exp` | Expiry time |
| `jti` | Random token ID |
| `token_type` | `access` or `refresh` |
| `sid` | Session ID |
| `email` | User email |

The decoder validates signature, expiry, and issuer. The token type must match the operation
(`access` for API calls, `refresh` for refresh). The authentication filter additionally checks that
the `sid` row still exists, so deleting the session revokes all access tokens bound to it.

### Sign-in session lifecycle

```text
sign-in ──► create session(id, sha256(refreshToken), expiresAt, ip, userAgent)
        ──► issue access token (sid) + refresh token (sid)

refresh ──► parse refresh JWT
        ──► lookup session by sha256(token)
        ──► verify subject + sid match
        ──► replace stored hash and expiry with a new refresh token
        ──► issue new access token

sign-out ─► delete session by refresh-token hash or by access-token sid
```

---

## Email verification flow

1. On sign-up, `VerificationService` deletes any previous verification for the email, generates a
   numeric code (`VERIFICATION_CODE_LENGTH`, default 6), and stores **SHA-256(code)** with an expiry
   (`VERIFICATION_TTL`, default 24 hours).
2. `EmailService` renders `templates/email/verify-account.html` with the recipient name, code, expiry,
   and a verification URL.
3. The user submits the code to `POST /api/auth/verify` or clicks the link (`GET /api/auth/verify`).
4. The code is compared in constant time against the stored hash. Expired codes are deleted.
5. On success, the user is marked verified and the verification row is deleted.

Resending replaces the previous code. Verification rows are single-use per attempt set.

---

## Database schema

Flyway migrations live in `src/main/resources/db/migration` and run automatically at startup.
Hibernate runs in `validate` mode, so entities and migrations must stay in sync.

### `users`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `VARCHAR(36)` PK | UUID |
| `name` | `VARCHAR(255)` | Required |
| `email` | `VARCHAR(255)` | Required, unique, lowercase |
| `emailVerified` | `BOOLEAN` | Required |
| `image` | `TEXT` | Optional avatar URL |
| `createdAt`, `updatedAt` | `TIMESTAMPTZ` | Managed by JPA |

### `accounts`

Credential and (future) OAuth accounts. One row per provider per user.

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `VARCHAR(36)` PK | UUID |
| `userId` | `VARCHAR(36)` FK → `users.id` | `ON DELETE CASCADE` |
| `accountId` | `VARCHAR(255)` | For credentials, the user ID |
| `providerId` | `VARCHAR(255)` | `credential` for email/password |
| `accessToken`, `refreshToken`, `idToken`, `scope` | `TEXT` | Reserved for OAuth providers |
| `accessTokenExpiresAt`, `refreshTokenExpiresAt` | `TIMESTAMPTZ` | Reserved for OAuth providers |
| `password` | `TEXT` | BCrypt hash for credentials |

### `sessions`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `VARCHAR(36)` PK | Also the JWT `sid` |
| `userId` | `VARCHAR(36)` FK → `users.id` | `ON DELETE CASCADE` |
| `token` | `TEXT` | Unique SHA-256 of the current refresh token |
| `expiresAt` | `TIMESTAMPTZ` | Refresh-token expiry |
| `ipAddress` | `VARCHAR(45)` | From `X-Forwarded-For` or remote address |
| `userAgent` | `TEXT` | Client user agent |

### `verifications`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `VARCHAR(36)` PK | UUID |
| `userId` | `VARCHAR(36)` FK → `users.id` | `ON DELETE CASCADE` |
| `identifier` | `VARCHAR(255)` | Normalized email |
| `value` | `TEXT` | SHA-256 of the code |
| `expiresAt` | `TIMESTAMPTZ` | Code expiry |

### Authorization and tenancy

| Table | Purpose |
| --- | --- |
| `roles` | Named roles with description and `superAdminOnly` flag |
| `permissions` | Named permissions with description and `superAdminOnly` flag |
| `users_roles` | User ↔ role many-to-many |
| `roles_permissions` | Role ↔ permission many-to-many |
| `enterprises` | Enterprise owned by a user (`ownerId`), with ISO-4217 `currency` |
| `stores` | Store belonging to an enterprise (`location`) |
| `enterprises_users` | Enterprise membership many-to-many |
| `store_users` | Store membership many-to-many |
| `flyway_schema_history` | Migration bookkeeping |

All foreign keys use `ON DELETE CASCADE`. Indexes exist on foreign-key columns.

---

## Error reference

| `code` | HTTP | Meaning |
| --- | --- | --- |
| `VALIDATION_FAILED` | `400` | Bean validation / request-parameter validation failed |
| `INVALID_REQUEST_BODY` | `400` | Body missing, malformed, or unreadable JSON |
| `MISSING_PARAMETER` | `400` | Required query parameter missing |
| `VERIFICATION_FAILED` | `400` | Verification code invalid or expired |
| `INVALID_CREDENTIALS` | `401` | Wrong email or password |
| `INVALID_TOKEN` | `401` | JWT invalid, expired, wrong type, revoked, or missing |
| `UNAUTHORIZED` | `401` | Authentication required |
| `EMAIL_NOT_VERIFIED` | `403` | Account exists but email is unverified |
| `ACCESS_DENIED` | `403` | Authenticated but not permitted |
| `RESOURCE_NOT_FOUND` | `404` | No route/resource |
| `METHOD_NOT_ALLOWED` | `405` | HTTP method unsupported for route |
| `EMAIL_ALREADY_EXISTS` | `409` | Duplicate account email |
| `DATA_INTEGRITY_VIOLATION` | `409` | Database constraint conflict |
| `UNSUPPORTED_MEDIA_TYPE` | `415` | Invalid `Content-Type` |
| `EMAIL_SENDING_FAILED` | `500` | SMTP delivery failed (sign-up logs this and still returns `201`) |
| `INTERNAL_SERVER_ERROR` | `500` | Unhandled server error |

---

## Docker

The `Dockerfile` uses three stages:

1. **`build`** — `maven:3.10-eclipse-temurin-25-alpine` compiles and packages the jar with
   dependency caching via `dependency:go-offline`.
2. **`jre`** — `eclipse-temurin:25-jre-alpine` provides the Alpine (musl) JRE, creates the
   unprivileged `kora` user (UID/GID `10001`), and prepares a world-writable `/tmp`.
3. **`runtime`** — `scratch`, populated only with the JRE, musl loader, zlib, passwd/group entries,
   `/tmp`, and the application jar. There is **no shell, no package manager, and no OS tooling**.

Result: a minimal Alpine-based, distroless runtime that runs as a non-root user.

Build and run:

```bash
docker build -t kora-backend:latest .

# Docker Desktop (Windows/macOS): host.docker.internal reaches the host
docker run --rm -p 8080:8080 \
  -e DB_URL='jdbc:postgresql://host.docker.internal:5432/postgres' \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e MAIL_HOST=host.docker.internal \
  -e MAIL_PORT=1025 \
  -e JWT_SECRET='replace-with-a-random-32-byte-or-longer-secret' \
  kora-backend:latest
```

On Linux, add `--add-host=host.docker.internal:host-gateway` or use a user-defined Docker network
with PostgreSQL in it.

Inspect the image (there is intentionally no shell):

```bash
docker image inspect kora-backend:latest --format '{{.Config.User}} {{.Config.Entrypoint}}'
docker run --rm --entrypoint /opt/java/openjdk/bin/java kora-backend:latest -version
```

`SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, and all variables in
[Configuration](#configuration) can be passed with `-e`.

The container exposes port `8080`. Because it is distroless, configure health checks at the
orchestrator level against `GET /actuator/health` rather than relying on an in-image `curl` or shell.

---

## Continuous integration and delivery

`.github/workflows/ci.yml` runs on pushes, pull requests, version tags, and manual dispatch.

**Job `test`**

1. Checks out the repository.
2. Sets up Temurin JDK 25 with Maven caching.
3. Starts a PostgreSQL 17 service container.
4. Runs `./mvnw -B -ntp verify` (compile + tests against the service database).
5. Asserts that `target/openapi.yaml` was generated, contains the expected paths, and uploads it as
   the `openapi-spec` artifact.
6. Uploads Surefire reports and the application jar as artifacts.

**Job `docker`**

1. Builds the image with Buildx and GitHub Actions layer caching.
2. Loads it locally and asserts it runs as UID/GID `10001:10001` with no `/bin/sh`.
3. Starts PostgreSQL and runs the container on the host network.
4. Polls `GET /actuator/health` until it reports `"status":"UP"`.
5. Verifies that `/v3/api-docs.yaml` is served and that a protected endpoint
   (`/api/auth/refresh`) returns `401` without a token.

**Job `publish`**

Runs only for pushes to `main` and `v*.*.*` tags, after `test` and `docker` succeed:

1. Logs in to GitHub Container Registry with the workflow `GITHUB_TOKEN`.
2. Generates tags from branch, semver tag, and commit SHA via `docker/metadata-action`.
3. Pushes to `ghcr.io/<owner>/<repo>` (for example
   `ghcr.io/georgesnoe/kora-backend:latest`).

Required permissions are declared in the workflow (`contents: read`, `packages: write`). No custom
secret is needed; `GITHUB_TOKEN` is provided by GitHub.

---

## Security notes

- Store `JWT_SECRET` in a secret manager; never commit production secrets. Minimum length is
  enforced in code (32 bytes).
- Access tokens are short-lived; refresh tokens rotate and are stored only as SHA-256 hashes.
- `/api/auth/refresh`, `/api/auth/sign-out`, and `/api/auth/resend-verification` require a valid
  access token. Email-verification status is not part of the authorization decision.
- Because refresh requires a valid access token, a client whose access token has expired must sign
  in again rather than refreshing.
- Deleting a session revokes both token types because access-token authentication checks the session.
- Passwords are hashed with BCrypt. Password length is capped at 72 characters (BCrypt limit).
- Resend-verification responses do not reveal whether an account exists.
- Verification codes are compared in constant time.
- Sign-in accepts `X-Forwarded-For`; only trust this header when a trusted reverse proxy sets it.
- The service is stateless and CSRF-disabled by design: do not add cookie-based authentication
  without reintroducing CSRF protection.
- `/v3/api-docs/**` and `/swagger-ui/**` are public. Disable them in production with
  `SPRINGDOC_API_DOCS_ENABLED=false` and `SPRINGDOC_SWAGGER_UI_ENABLED=false` if they must not be
  exposed.
- Run behind TLS in production. The distroless image contains no shell, which reduces the attack
  surface for remote code execution.

---

## Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| Startup fails with `app.jwt.secret must be at least 32 bytes` | Set a longer `JWT_SECRET` |
| Startup fails with `Connection refused` / Flyway error | PostgreSQL is not running or `DB_URL` is wrong |
| Startup fails with Hibernate `validate` error | Entity and migration drift; add a Flyway migration |
| Sign-in returns `403 EMAIL_NOT_VERIFIED` | Verify the email with the code from Mailpit |
| No email arrives | Check `MAIL_HOST`/`MAIL_PORT`; inspect Mailpit at <http://localhost:8025> |
| `401 INVALID_TOKEN` on refresh | Refresh token expired, rotated already, or session signed out |
| `401 UNAUTHORIZED` on protected routes | Missing/expired access token, or session revoked |
| Docker container exits immediately | Check `docker logs`; usually missing DB connectivity or `JWT_SECRET` |
| Tests fail locally | Start `docker compose up -d` first (the context test needs PostgreSQL) |

---

## License

No license has been declared for this repository. All rights reserved by the author unless a
license is added.
