ALTER TABLE "vacancy"
    ADD COLUMN company_id VARCHAR(12) NOT NULL;

ALTER TABLE "vacancy"
    ADD COLUMN area_id VARCHAR(12);

ALTER TABLE "vacancy"
    ADD CONSTRAINT fk_vacancy_company
        FOREIGN KEY (company_id) REFERENCES company(company_id);

ALTER TABLE "vacancy"
    ADD CONSTRAINT fk_vacancy_area
        FOREIGN KEY (area_id) REFERENCES area(area_id);