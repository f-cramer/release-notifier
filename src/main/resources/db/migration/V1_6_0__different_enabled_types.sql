ALTER TABLE "bsto_series"
	ADD "enabler_type" VARCHAR(15) NOT NULL DEFAULT 'boolean';
ALTER TABLE "bsto_series"
	ADD "enabler_boolean_value" BOOLEAN NULL DEFAULT true;
ALTER TABLE "bsto_series"
	ADD "enabler_date_between_start" DATE NULL;
ALTER TABLE "bsto_series"
	ADD "enabler_date_between_end" DATE NULL;

UPDATE "bsto_series"
SET "enabler_boolean_value" = "enabled";

ALTER TABLE "bsto_series"
	DROP "enabled";


ALTER TABLE "downmagaz_magazines"
	ADD "enabler_type" VARCHAR(15) NOT NULL DEFAULT 'boolean';
ALTER TABLE "downmagaz_magazines"
	ADD "enabler_boolean_value" BOOLEAN NULL DEFAULT true;
ALTER TABLE "downmagaz_magazines"
	ADD "enabler_date_between_start" DATE NULL;
ALTER TABLE "downmagaz_magazines"
	ADD "enabler_date_between_end" DATE NULL;

UPDATE "downmagaz_magazines"
SET "enabler_boolean_value" = "enabled";

ALTER TABLE "downmagaz_magazines"
	DROP "enabled";


ALTER TABLE "jackett_searches"
	ADD "enabler_type" VARCHAR(15) NOT NULL DEFAULT 'boolean';
ALTER TABLE "jackett_searches"
	ADD "enabler_boolean_value" BOOLEAN NULL DEFAULT true;
ALTER TABLE "jackett_searches"
	ADD "enabler_date_between_start" DATE NULL;
ALTER TABLE "jackett_searches"
	ADD "enabler_date_between_end" DATE NULL;

UPDATE "jackett_searches"
SET "enabler_boolean_value" = "enabled";

ALTER TABLE "jackett_searches"
	DROP "enabled";


ALTER TABLE "tabletoptactics_configurations"
	ADD "enabler_type" VARCHAR(15) NOT NULL DEFAULT 'boolean';
ALTER TABLE "tabletoptactics_configurations"
	ADD "enabler_boolean_value" BOOLEAN NULL DEFAULT true;
ALTER TABLE "tabletoptactics_configurations"
	ADD "enabler_date_between_start" DATE NULL;
ALTER TABLE "tabletoptactics_configurations"
	ADD "enabler_date_between_end" DATE NULL;

UPDATE "tabletoptactics_configurations"
SET "enabler_boolean_value" = "enabled";

ALTER TABLE "tabletoptactics_configurations"
	DROP "enabled";
