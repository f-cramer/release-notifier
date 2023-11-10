ALTER TABLE "bsto_series"
	ADD COLUMN "enabled" BOOLEAN NULL;

UPDATE "bsto_series"
SET "enabled" = true;

ALTER TABLE "bsto_series"
	ALTER COLUMN "enabled" SET NOT NULL;


ALTER TABLE "downmagaz_magazines"
	ADD COLUMN "enabled" BOOLEAN NULL;

UPDATE "downmagaz_magazines"
SET "enabled" = true;

ALTER TABLE "downmagaz_magazines"
	ALTER COLUMN "enabled" SET NOT NULL;


ALTER TABLE "jackett_searches"
	ADD COLUMN "enabled" BOOLEAN NULL;

UPDATE "jackett_searches"
SET "enabled" = true;

ALTER TABLE "jackett_searches"
	ALTER COLUMN "enabled" SET NOT NULL;


ALTER TABLE "tabletoptactics_configurations"
	ADD COLUMN "enabled" BOOLEAN NULL;

UPDATE "tabletoptactics_configurations"
SET "enabled" = true;

ALTER TABLE "tabletoptactics_configurations"
	ALTER COLUMN "enabled" SET NOT NULL;
