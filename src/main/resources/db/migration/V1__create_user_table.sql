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
