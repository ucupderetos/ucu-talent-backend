CREATE TABLE company (
    company_id    VARCHAR(12)  NOT NULL,
    user_id       VARCHAR(12)  NOT NULL,
    industry      VARCHAR(255) NOT NULL,
    description   TEXT         NOT NULL,
    web_url       VARCHAR(255) NOT NULL,
    linkedin_url  VARCHAR(255) NOT NULL,
    location      VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_company PRIMARY KEY (company_id),

    CONSTRAINT uq_company_user UNIQUE (user_id),

    CONSTRAINT fk_company_user FOREIGN KEY (user_id) REFERENCES "user"(user_id),

    CONSTRAINT ck_company_location CHECK (location IN (
        'ARTIGAS', 'CANELONES', 'CERRO_LARGO', 'COLONIA', 'DURAZNO', 'FLORES', 'FLORIDA',
        'LAVALLEJA', 'MALDONADO', 'MONTEVIDEO', 'PAYSANDU', 'RIO_NEGRO', 'RIVERA', 'ROCHA',
        'SALTO', 'SAN_JOSE', 'SORIANO', 'TACUAREMBO', 'TREINTA_Y_TRES'))
);
