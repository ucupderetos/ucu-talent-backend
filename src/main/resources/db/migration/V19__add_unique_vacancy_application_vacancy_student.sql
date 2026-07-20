-- Una postulacion unica por par (vacancy, student), regla de negocio de CLAUDE.md que
-- faltaba enforced a nivel de BD (V14 solo tenia las FKs, sin UNIQUE).
ALTER TABLE vacancy_application
    ADD CONSTRAINT uq_vacancy_application_vacancy_student UNIQUE (vacancy_id, student_profile_id);
