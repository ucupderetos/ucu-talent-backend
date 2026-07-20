ALTER TABLE "vacancy"
    ADD COLUMN reviewed_by VARCHAR(12);

ALTER TABLE "vacancy"
    ADD CONSTRAINT fk_vacancy_reviewed_by
        FOREIGN KEY (reviewed_by)
            REFERENCES "user"(user_id);