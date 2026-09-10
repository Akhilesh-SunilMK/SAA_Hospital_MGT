-- hms_auth schema (SRS 8.1, pragmatically simplified: the 8 roles in Appendix B are a fixed
-- enum, not admin-managed data, so no separate roles/permissions lookup tables are modeled —
-- permissions are stored per-user, seeded by the UserFactory that created the account).

CREATE TABLE users (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    username               VARCHAR(100)  NOT NULL,
    email                  VARCHAR(150)  NOT NULL,
    password_hash          VARCHAR(255)  NOT NULL,
    first_name             VARCHAR(100)  NOT NULL,
    last_name              VARCHAR(100)  NOT NULL,
    role                   VARCHAR(30)   NOT NULL,
    enabled                BOOLEAN       NOT NULL DEFAULT TRUE,
    locked                 BOOLEAN       NOT NULL DEFAULT FALSE,
    failed_login_attempts  INT           NOT NULL DEFAULT 0,
    locked_until           DATETIME      NULL,
    created_at             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_permissions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    permission  VARCHAR(100) NOT NULL,
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_user_permissions UNIQUE (user_id, permission)
);

CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  DATETIME     NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash)
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id, revoked);

CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  DATETIME     NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_password_reset_tokens_hash UNIQUE (token_hash)
);
