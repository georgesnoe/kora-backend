CREATE TABLE IF NOT EXISTS "enterprises" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "ownerId" VARCHAR(36) NOT NULL,
  "name" VARCHAR(255) NOT NULL,
  "currency" VARCHAR(3) NOT NULL,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL,
  FOREIGN KEY ("ownerId") REFERENCES "users" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "enterprises_ownerId_idx" ON "enterprises" ("ownerId");
CREATE TABLE IF NOT EXISTS "stores" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "enterpriseId" VARCHAR(36) NOT NULL,
  "location" VARCHAR(255) NOT NULL,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL,
  FOREIGN KEY ("enterpriseId") REFERENCES "enterprises" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "stores_enterpriseId_idx" ON "stores" ("enterpriseId");
CREATE TABLE IF NOT EXISTS "enterprises_users" (
  "enterpriseId" VARCHAR(36) NOT NULL,
  "userId" VARCHAR(36) NOT NULL,
  PRIMARY KEY ("enterpriseId", "userId"),
  FOREIGN KEY ("enterpriseId") REFERENCES "enterprises" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "enterprises_users_userId_idx" ON "enterprises_users" ("userId");
CREATE TABLE IF NOT EXISTS "store_users" (
  "storeId" VARCHAR(36) NOT NULL,
  "userId" VARCHAR(36) NOT NULL,
  PRIMARY KEY ("storeId", "userId"),
  FOREIGN KEY ("storeId") REFERENCES "stores" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "store_users_userId_idx" ON "store_users" ("userId");
