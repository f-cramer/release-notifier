ALTER TABLE "jackett_releases"
	ADD COLUMN "creation_timestamp" TIMESTAMP NULL;

UPDATE "jackett_releases"
SET "creation_timestamp" = now();

ALTER TABLE "jackett_releases"
	ALTER COLUMN "creation_timestamp" SET NOT NULL;
