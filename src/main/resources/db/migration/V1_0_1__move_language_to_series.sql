ALTER TABLE "series"
	ADD "language" VARCHAR(10) NULL;

UPDATE "series" s
SET "language" = (SELECT DISTINCT ("language")
				  FROM "seasons"
				  WHERE "seasons"."series_id" = s."series_id");

ALTER TABLE "series"
	ALTER "language" SET NOT NULL;

ALTER TABLE "seasons"
	DROP "language";
