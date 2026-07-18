-- Renombra la PK de work_experience de 'id' a 'work_experience_id' para
-- alinear la columna con el campo workExperienceId de la entidad (ddl-auto=validate).
-- Renombrar la columna arrastra automaticamente la constraint pk_work_experience.
ALTER TABLE "work_experience" RENAME COLUMN id TO work_experience_id;
