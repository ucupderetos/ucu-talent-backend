-- Alinea el enum DocumentType de university_registry con el de User: CEDULA -> CEDULA_IDENTIDAD.
-- 1) migra las filas existentes que tengan el valor viejo.
UPDATE university_registry
    SET document_type = 'CEDULA_IDENTIDAD'
    WHERE document_type = 'CEDULA';

-- 2) reemplaza la CHECK constraint por una con el valor nuevo.
ALTER TABLE university_registry
    DROP CONSTRAINT ck_university_registry_document_type;

ALTER TABLE university_registry
    ADD CONSTRAINT ck_university_registry_document_type
    CHECK (document_type IN ('CEDULA_IDENTIDAD', 'DNI', 'PASAPORTE'));
