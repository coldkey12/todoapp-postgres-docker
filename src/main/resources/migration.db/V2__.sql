ALTER TABLE products
    ADD available BOOLEAN;

ALTER TABLE products
    ADD price DOUBLE PRECISION;

ALTER TABLE products
    ALTER COLUMN available SET NOT NULL;

ALTER TABLE products
    ALTER COLUMN price SET NOT NULL;

ALTER TABLE user_transcation
    ADD quantity INTEGER;

ALTER TABLE user_transcation
    ADD transaction_status VARCHAR(255);

ALTER TABLE user_transcation
    ALTER COLUMN quantity SET NOT NULL;

ALTER TABLE users
    ADD some_third_party_payment_service_wallet_id VARCHAR(255);

ALTER TABLE users_aud
    ADD some_third_party_payment_service_wallet_id VARCHAR(255);

ALTER TABLE user_transcation
    ALTER COLUMN transaction_status SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT uc_products_title UNIQUE (title);

ALTER TABLE users
    ADD CONSTRAINT uc_users_somethirdpartypaymentservicewalletid UNIQUE (some_third_party_payment_service_wallet_id);

ALTER TABLE user_transcation
    ALTER COLUMN user_id DROP NOT NULL;