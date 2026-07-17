CREATE TABLE university_registry (
    university_registry_id  VARCHAR(12)  NOT NULL,
    document_type           VARCHAR(20)  NOT NULL,
    document_number         VARCHAR(20)  NOT NULL,
    name                    VARCHAR(50)  NOT NULL,
    surname                 VARCHAR(50)  NOT NULL,

    CONSTRAINT pk_university_registry PRIMARY KEY (university_registry_id),
    CONSTRAINT ck_university_registry_document_type
        CHECK (document_type IN ('CEDULA', 'DNI', 'PASAPORTE'))
);
