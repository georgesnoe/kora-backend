CREATE TABLE IF NOT EXISTS "roles" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL UNIQUE,
  "description" TEXT,
  "superAdminOnly" BOOLEAN NOT NULL DEFAULT false,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL
);
CREATE TABLE IF NOT EXISTS "permissions" (
  "id" VARCHAR(36) NOT NULL PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL UNIQUE,
  "description" TEXT,
  "superAdminOnly" BOOLEAN NOT NULL DEFAULT false,
  "createdAt" TIMESTAMPTZ NOT NULL,
  "updatedAt" TIMESTAMPTZ NOT NULL
);
CREATE TABLE IF NOT EXISTS "users_roles" (
  "userId" VARCHAR(36) NOT NULL,
  "roleId" VARCHAR(36) NOT NULL,
  PRIMARY KEY ("userId", "roleId"),
  FOREIGN KEY ("userId") REFERENCES "users" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("roleId") REFERENCES "roles" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "users_roles_roleId_idx" ON "users_roles" ("roleId");
CREATE TABLE IF NOT EXISTS "roles_permissions" (
  "roleId" VARCHAR(36) NOT NULL,
  "permissionId" VARCHAR(36) NOT NULL,
  PRIMARY KEY ("roleId", "permissionId"),
  FOREIGN KEY ("roleId") REFERENCES "roles" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("permissionId") REFERENCES "permissions" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "roles_permissions_permissionId_idx" ON "roles_permissions" ("permissionId");
