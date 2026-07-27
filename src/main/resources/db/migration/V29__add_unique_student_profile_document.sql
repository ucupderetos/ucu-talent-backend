-- RN-17: documento unico entre alumnos. El par (document_type, document_number) no puede
-- repetirse, pero el mismo numero puede existir con document_type distinto
-- (ej. "12345678" como CEDULA_IDENTIDAD de un alumno y como DNI de otro).
ALTER TABLE student_profile
    ADD CONSTRAINT uq_student_profile_document UNIQUE (document_type, document_number);
