ALTER TABLE "vacancy" RENAME COLUMN locality TO location;
ALTER TABLE "vacancy" ALTER COLUMN location DROP DEFAULT;