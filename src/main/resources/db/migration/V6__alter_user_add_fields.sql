ALTER TABLE "user"
    ADD COLUMN surname          VARCHAR(50)  NOT NULL DEFAULT '',
    ADD COLUMN phone_number     VARCHAR(20),
    ADD COLUMN document_type    VARCHAR(20),
    ADD COLUMN document_number  VARCHAR(20),
    ADD COLUMN linkedin_url     VARCHAR(255);

ALTER TABLE "user" ALTER COLUMN surname DROP DEFAULT;

ALTER TABLE "user" ADD CONSTRAINT ck_user_document_type
    CHECK (document_type IN ('CEDULA_IDENTIDAD', 'PASAPORTE', 'DNI'));

ALTER TABLE "user" ALTER COLUMN name TYPE VARCHAR(50);
ALTER TABLE "user" ALTER COLUMN password_hash TYPE VARCHAR(60);
