CREATE TABLE wallets
(
    id       VARCHAR(255)     NOT NULL,
    balance  DOUBLE PRECISION NOT NULL,
    currency VARCHAR(255),
    user_id  UUID,
    CONSTRAINT pk_wallets PRIMARY KEY (id)
);

CREATE TABLE wallets_aud
(
    rev      INTEGER      NOT NULL,
    revtype  SMALLINT,
    id       VARCHAR(255) NOT NULL,
    balance  DOUBLE PRECISION,
    currency VARCHAR(255),
    user_id  UUID,
    CONSTRAINT pk_wallets_aud PRIMARY KEY (rev, id)
);

ALTER TABLE users
    ADD wallet_id VARCHAR(255);

ALTER TABLE users_aud
    ADD wallet_id VARCHAR(255);

ALTER TABLE users
    ADD CONSTRAINT uc_users_wallet UNIQUE (wallet_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallets (id);

ALTER TABLE wallets_aud
    ADD CONSTRAINT FK_WALLETS_AUD_ON_REV FOREIGN KEY (rev) REFERENCES audit_envers_info (id);

ALTER TABLE wallets
    ADD CONSTRAINT FK_WALLETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users
    DROP COLUMN some_third_party_payment_service_wallet_id;