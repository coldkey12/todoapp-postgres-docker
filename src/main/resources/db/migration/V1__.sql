CREATE SEQUENCE IF NOT EXISTS audit_envers_info_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE audit_envers_info
(
    id        INTEGER NOT NULL,
    timestamp BIGINT  NOT NULL,
    username  VARCHAR(255),
    CONSTRAINT pk_audit_envers_info PRIMARY KEY (id)
);

CREATE TABLE bmw
(
    id        UUID    NOT NULL,
    model     VARCHAR(255),
    ismseries BOOLEAN NOT NULL,
    CONSTRAINT pk_bmw PRIMARY KEY (id)
);

CREATE TABLE carts
(
    id          UUID                        NOT NULL,
    user_id     UUID                        NOT NULL,
    iin         VARCHAR(255),
    first_name  VARCHAR(255),
    second_name VARCHAR(255),
    patronymic  VARCHAR(255),
    address     VARCHAR(255),
    number      VARCHAR(255),
    is_deleted  BOOLEAN                     NOT NULL,
    is_approved BOOLEAN                     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_carts PRIMARY KEY (id)
);

CREATE TABLE mercedes_benz
(
    id      UUID NOT NULL,
    model   VARCHAR(255),
    mileage VARCHAR(255),
    CONSTRAINT pk_mercedesbenz PRIMARY KEY (id)
);

CREATE TABLE products
(
    id          UUID                        NOT NULL,
    quantity    BIGINT,
    price       DOUBLE PRECISION            NOT NULL,
    available   BOOLEAN                     NOT NULL,
    title       VARCHAR(100)                NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE refresh_tokens
(
    id          UUID                        NOT NULL,
    token       VARCHAR(255)                NOT NULL,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id     UUID,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
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
    id                 UUID                        NOT NULL,
    user_id            UUID,
    transaction_status VARCHAR(255)                NOT NULL,
    quantity           INTEGER                     NOT NULL,
    product_id         UUID                        NOT NULL,
    card_id            UUID,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_user_transcation PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         UUID                        NOT NULL,
    username   VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    wallet_id  VARCHAR(255),
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
    wallet_id  VARCHAR(255),
    role       VARCHAR(255),
    full_name  VARCHAR(255),
    enabled    BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_users_aud PRIMARY KEY (rev, id)
);

CREATE TABLE wallets
(
    id         VARCHAR(255)                NOT NULL,
    balance    DOUBLE PRECISION            NOT NULL,
    currency   VARCHAR(255),
    user_id    UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_wallets PRIMARY KEY (id)
);

CREATE TABLE wallets_aud
(
    rev        INTEGER      NOT NULL,
    revtype    SMALLINT,
    id         VARCHAR(255) NOT NULL,
    balance    DOUBLE PRECISION,
    currency   VARCHAR(255),
    user_id    UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_wallets_aud PRIMARY KEY (rev, id)
);

ALTER TABLE products
    ADD CONSTRAINT uc_products_title UNIQUE (title);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_token UNIQUE (token);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_user UNIQUE (user_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE users
    ADD CONSTRAINT uc_users_wallet UNIQUE (wallet_id);

ALTER TABLE carts
    ADD CONSTRAINT FK_CARTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE tasks_aud
    ADD CONSTRAINT FK_TASKS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES audit_envers_info (id);

ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_aud
    ADD CONSTRAINT FK_USERS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES audit_envers_info (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallets (id);

ALTER TABLE user_transcation
    ADD CONSTRAINT FK_USER_TRANSCATION_ON_CARD FOREIGN KEY (card_id) REFERENCES carts (id);

ALTER TABLE user_transcation
    ADD CONSTRAINT FK_USER_TRANSCATION_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE user_transcation
    ADD CONSTRAINT FK_USER_TRANSCATION_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE wallets_aud
    ADD CONSTRAINT FK_WALLETS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES audit_envers_info (id);

ALTER TABLE wallets
    ADD CONSTRAINT FK_WALLETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);