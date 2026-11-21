-- Link a library member / librarian to their Keycloak login.
-- Nullable: rows created before this have no login.
ALTER TABLE members
    ADD COLUMN keycloak_user_id VARCHAR(36) NULL AFTER role,
    ADD CONSTRAINT uk_members_keycloak_user_id UNIQUE (keycloak_user_id);
