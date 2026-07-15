-- "user" es palabra reservada en Postgres, va entre comillas dobles (igual que en @Table de la entidad)
CREATE TABLE "user" (
    user_id        VARCHAR(12)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    registered_at  DATE         NOT NULL,

    CONSTRAINT pk_user PRIMARY KEY (user_id),
    CONSTRAINT uq_user_email UNIQUE (email),
    CONSTRAINT ck_user_role CHECK (role IN ('ALUMNO', 'EMPRESA', 'ADMIN'))
);

CREATE TABLE "vacancy" (
     vacancy_id VARCHAR(12) PRIMARY KEY,
     publication_date DATE NOT NULL,
     closing_date DATE,
     locality VARCHAR(30),
     modality VARCHAR(20) NOT NULL,
     status VARCHAR(20) NOT NULL,
     name VARCHAR(120) NOT NULL,
     description TEXT,
     requirements TEXT,
     contract_type VARCHAR(80),
     salary_range VARCHAR(80)

    -- Cuando existan las entidades:
    -- company_id VARCHAR(12) NOT NULL,
    -- area_id VARCHAR(12) NOT NULL,

    -- CONSTRAINT fk_vacancy_company
    --     FOREIGN KEY (company_id) REFERENCES company(company_id),

    -- CONSTRAINT fk_vacancy_area
    --     FOREIGN KEY (area_id) REFERENCES area(area_id)
);
