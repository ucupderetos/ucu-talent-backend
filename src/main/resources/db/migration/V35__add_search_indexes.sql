-- Postgres no indexa las FOREIGN KEY automaticamente (solo PK y UNIQUE).
CREATE INDEX idx_vacancy_company_id ON vacancy (company_id) WHERE deleted = false;

CREATE INDEX idx_vacancy_area_id ON vacancy (area_id) WHERE deleted = false;


CREATE INDEX idx_vacancy_publication_date ON vacancy (publication_date DESC) WHERE deleted = false;

CREATE INDEX idx_education_student_profile_id ON education (student_profile_id);

CREATE INDEX idx_work_experience_student_profile_id ON work_experience (student_profile_id);

CREATE INDEX idx_area_parent_area_id ON area (parent_area_id);

CREATE INDEX idx_degree_area_id ON degree (area_id);
