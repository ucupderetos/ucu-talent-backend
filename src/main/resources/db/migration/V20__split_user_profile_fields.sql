ALTER TABLE "user" ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';

ALTER TABLE "user" ADD CONSTRAINT ck_user_status
    CHECK (status IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'));



ALTER TABLE student_profile
    ADD COLUMN name             VARCHAR(50),
    ADD COLUMN surname          VARCHAR(50),
    ADD COLUMN document_type    VARCHAR(20),
    ADD COLUMN document_number  VARCHAR(20),
    ADD COLUMN phone_number     VARCHAR(20),
    ADD COLUMN linkedin_url     VARCHAR(255);


ALTER TABLE student_profile
    ALTER COLUMN name            SET NOT NULL,
    ALTER COLUMN surname         SET NOT NULL,
    ALTER COLUMN document_type   SET NOT NULL,
    ALTER COLUMN document_number SET NOT NULL;

ALTER TABLE student_profile ADD CONSTRAINT ck_student_profile_document_type
    CHECK (document_type IN ('CEDULA_IDENTIDAD', 'PASAPORTE', 'DNI'));


ALTER TABLE company ADD COLUMN name VARCHAR(255);


ALTER TABLE company ALTER COLUMN name SET NOT NULL;

-- PK compartida real: la PK de cada perfil pasa a ser tambien FK a "user".
ALTER TABLE student_profile DROP CONSTRAINT fk_student_profile_user;
ALTER TABLE student_profile DROP CONSTRAINT uq_student_profile_user;
ALTER TABLE student_profile ADD CONSTRAINT fk_student_profile_user
    FOREIGN KEY (student_profile_id) REFERENCES "user"(user_id) ON DELETE CASCADE;

ALTER TABLE company DROP CONSTRAINT fk_company_user;
ALTER TABLE company DROP CONSTRAINT uq_company_user;
ALTER TABLE company ADD CONSTRAINT fk_company_user
    FOREIGN KEY (company_id) REFERENCES "user"(user_id) ON DELETE CASCADE;

ALTER TABLE student_profile DROP COLUMN user_id;
ALTER TABLE company DROP COLUMN user_id;
ALTER TABLE company DROP COLUMN approved;

ALTER TABLE "user" DROP CONSTRAINT ck_user_document_type;

ALTER TABLE "user"
    DROP COLUMN name,
    DROP COLUMN surname,
    DROP COLUMN phone_number,
    DROP COLUMN document_type,
    DROP COLUMN document_number,
    DROP COLUMN linkedin_url;

ALTER TABLE "user" ALTER COLUMN status DROP DEFAULT;
