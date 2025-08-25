CREATE TABLE bmw
(
    id        UUID    NOT NULL,
    model     VARCHAR(255),
    ismseries BOOLEAN NOT NULL,
    CONSTRAINT pk_bmw PRIMARY KEY (id)
);

CREATE TABLE mercedes_benz
(
    id      UUID NOT NULL,
    model   VARCHAR(255),
    mileage VARCHAR(255),
    CONSTRAINT pk_mercedesbenz PRIMARY KEY (id)
);