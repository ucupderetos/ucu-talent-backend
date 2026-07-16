CREATE TABLE student_profile (
    student_profile_id  VARCHAR(12)  NOT NULL,
    user_id             VARCHAR(12)  NOT NULL,
    skills              JSONB,

    CONSTRAINT pk_student_profile PRIMARY KEY (student_profile_id),

    CONSTRAINT uq_student_profile_user UNIQUE (user_id),

    CONSTRAINT fk_student_profile_user FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);
