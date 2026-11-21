-- When the default first-login password of a librarian / member stops working (NULL = nothing pending)
ALTER TABLE members
    ADD COLUMN temp_password_expires_at DATETIME NULL AFTER keycloak_user_id;
