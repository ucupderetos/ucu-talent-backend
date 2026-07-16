
CREATE TABLE "work_experience" (
    id                  VARCHAR(12)  NOT NULL,
    student_profile_id  VARCHAR(12)  NOT NULL,
    company             VARCHAR(255),
    position            VARCHAR(255),
    start_date          DATE,
    end_date            DATE,
    description         TEXT,

    CONSTRAINT pk_work_experience PRIMARY KEY (id)
);
