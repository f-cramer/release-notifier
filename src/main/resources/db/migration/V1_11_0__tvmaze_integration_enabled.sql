ALTER TABLE "jackett_searches"
	ADD COLUMN "tvmaze_enabled" BOOLEAN NULL;

UPDATE "jackett_searches"
SET "tvmaze_enabled" = true
WHERE "tvmaze_show_id" IS NOT NULL;
