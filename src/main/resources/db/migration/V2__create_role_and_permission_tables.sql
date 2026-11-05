CREATE TABLE IF NOT EXISTS "role" (
    "id" text NOT NULL PRIMARY KEY,
    "name" text NOT NULL UNIQUE,
    "description" text,
    "superAdminOnly" boolean NOT NULL DEFAULT false,
    "createdAt" timestamptz NOT NULL,
    "updatedAt" timestamptz NOT NULL
);
CREATE TABLE IF NOT EXISTS "permission" (
    "id" text NOT NULL PRIMARY KEY,
    "name" text NOT NULL UNIQUE,
    "description" text,
    "superAdminOnly" boolean NOT NULL DEFAULT false,
    "createdAt" timestamptz NOT NULL,
    "updatedAt" timestamptz NOT NULL
);
CREATE TABLE IF NOT EXISTS "user_role" (
    "userId" text NOT NULL,
    "roleId" text NOT NULL,
    PRIMARY KEY ("userId", "roleId"),
    FOREIGN KEY ("userId") REFERENCES "user" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("roleId") REFERENCES "role" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "user_role_roleId_idx" ON "user_role" ("roleId");
CREATE TABLE IF NOT EXISTS "role_permission" (
    "roleId" text NOT NULL,
    "permissionId" text NOT NULL,
    PRIMARY KEY ("roleId", "permissionId"),
    FOREIGN KEY ("roleId") REFERENCES "role" ("id") ON DELETE CASCADE,
    FOREIGN KEY ("permissionId") REFERENCES "permission" ("id") ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS "role_permission_permissionId_idx" ON "role_permission" ("permissionId");