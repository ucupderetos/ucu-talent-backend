CREATE TABLE vacancy_application (
    vacancy_application_id VARCHAR(12)  NOT NULL,
    vacancy_id          VARCHAR(12)  NOT NULL,
    student_profile_id  VARCHAR(12)  NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    applied_at          DATE         NOT NULL,

    CONSTRAINT pk_vacancy_application PRIMARY KEY (vacancy_application_id),

    CONSTRAINT fk_vacancy_application_vacancy
        FOREIGN KEY (vacancy_id) REFERENCES vacancy(vacancy_id),

    CONSTRAINT fk_vacancy_application_student_profile
        FOREIGN KEY (student_profile_id) REFERENCES student_profile(student_profile_id),

    CONSTRAINT ck_vacancy_application_status CHECK (status IN ('PENDIENTE', 'VISTO', 'FINALIZADO'))
);
