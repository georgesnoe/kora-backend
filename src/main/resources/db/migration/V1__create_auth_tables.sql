CREATE TABLE IF NOT EXISTS "users" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL,
  "email" VARCHAR(255) NOT NULL UNIQUE,
  "emailVerified" BOOLEAN NOT NULL,
  "image" TEXT,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL
);
CREATE TABLE IF NOT EXISTS "sessions" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "userId" VARCHAR(36) NOT NULL,
  "token" TEXT NOT NULL UNIQUE,
  "expiresAt" TIMESTAMPTZ NOT NULL,
  "ipAddress" VARCHAR(45),
  "userAgent" TEXT,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "sessions_userId_idx" ON "sessions" ("userId");
CREATE TABLE IF NOT EXISTS "accounts" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "userId" VARCHAR(36) NOT NULL,
  "accountId" VARCHAR(255) NOT NULL,
  "providerId" VARCHAR(255) NOT NULL,
  "accessToken" TEXT,
  "refreshToken" TEXT,
  "accessTokenExpiresAt" TIMESTAMPTZ,
  "refreshTokenExpiresAt" TIMESTAMPTZ,
  "scope" TEXT,
  "idToken" TEXT,
  "password" TEXT,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "accounts_userId_idx" ON "accounts" ("userId");
CREATE TABLE IF NOT EXISTS "verifications" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "userId" VARCHAR(36) NOT NULL,
  "identifier" VARCHAR(255) NOT NULL,
  "value" TEXT NOT NULL,
  "expiresAt" TIMESTAMPTZ NOT NULL,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "verifications_identifier_idx" ON "verifications" ("identifier");
CREATE INDEX IF NOT EXISTS "verifications_userId_idx" ON "verifications" ("userId");
