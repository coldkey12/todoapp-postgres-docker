CREATE SEQUENCE IF NOT EXISTS audit_envers_info_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE audit_envers_info
(
    id        INTEGER NOT NULL,
    timestamp BIGINT  NOT NULL,
    username  VARCHAR(255),
    CONSTRAINT pk_audit_envers_info PRIMARY KEY (id)
);

CREATE TABLE products
(
    id          UUID         NOT NULL,
    quantity    BIGINT,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE refresh_tokens
(
    id          UUID                        NOT NULL,
    token       VARCHAR(255)                NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id     UUID,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

CREATE TABLE tasks
(
    id          UUID                        NOT NULL,
    title       VARCHAR(100)                NOT NULL,
    description VARCHAR(500),
    status      VARCHAR(255)                NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id     UUID                        NOT NULL,
    CONSTRAINT pk_tasks PRIMARY KEY (id)
);

CREATE TABLE tasks_aud
(
    rev         INTEGER NOT NULL,
    revtype     SMALLINT,
    id          UUID    NOT NULL,
    title       VARCHAR(100),
    description VARCHAR(500),
    status      VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    user_id     UUID,
    CONSTRAINT pk_tasks_aud PRIMARY KEY (rev, id)
);

CREATE TABLE user_transcation
(
    id         UUID NOT NULL,
    user_id    UUID NOT NULL,
    product_id UUID NOT NULL,
    CONSTRAINT pk_user_transcation PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                        NOT NULL,
    username   VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    role       VARCHAR(255)                NOT NULL,
    full_name  VARCHAR(255),
    enabled    BOOLEAN                     NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE users_aud
(
    rev        INTEGER NOT NULL,
    revtype    SMALLINT,
    id         UUID    NOT NULL,
    username   VARCHAR(255),
    password   VARCHAR(255),
    role       VARCHAR(255),
    full_name  VARCHAR(255),
    enabled    BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users_aud PRIMARY KEY (rev, id)
);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_token UNIQUE (token);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE tasks_aud
    ADD CONSTRAINT FK_TASKS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES audit_envers_info (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_aud
    ADD CONSTRAINT FK_USERS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES audit_envers_info (id);

ALTER TABLE user_transcation
    ADD CONSTRAINT FK_USER_TRANSCATION_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE user_transcation
    ADD CONSTRAINT FK_USER_TRANSCATION_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);