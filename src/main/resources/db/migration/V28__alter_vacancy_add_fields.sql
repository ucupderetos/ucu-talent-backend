ALTER TABLE "vacancy" RENAME COLUMN salary_range TO salary;

ALTER TABLE "vacancy" ALTER COLUMN location DROP NOT NULL;

ALTER TABLE "vacancy" ALTER COLUMN area_id SET NOT NULL;