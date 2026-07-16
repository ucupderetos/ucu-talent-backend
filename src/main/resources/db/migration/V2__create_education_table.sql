
CREATE TABLE "education" (
    education_id        VARCHAR(12)  NOT NULL,
    student_profile_id  VARCHAR(12)  NOT NULL,
    degree_level      VARCHAR(12)  NOT NULL,
    degree_id         VARCHAR(12) NOT NULL,
    description       TEXT,
    start_date      DATE         NOT NULL,
    end_date         DATE,

    CONSTRAINT pk_education PRIMARY KEY (education_id),
    CONSTRAINT ck_education_degree_level CHECK (degree_level IN ('TECNICATURA', 'LICENCIATURA', 'GRADO', 'POSGRADO', 'DOCTORADO'))
);
