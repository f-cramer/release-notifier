ALTER TABLE "jackett_searches"
	ADD COLUMN "tvmaze_show_id" BIGINT NULL;
ALTER TABLE "jackett_searches"
	ADD COLUMN "tvmaze_last_checked" DATE NULL;

CREATE TABLE "jackett_sub_searches"
(
	"sub_search_id"              BIGSERIAL    NOT NULL,
	"season"                     INT          NOT NULL,
	"episode"                    INT          NOT NULL,
	"url"                        VARCHAR(255) NOT NULL,
	"enabler_type"               VARCHAR(15)  NOT NULL,
	"enabler_boolean_value"      BOOLEAN      NULL,
	"enabler_date_between_start" DATE         NULL,
	"enabler_date_between_end"   DATE         NULL,
	"search_id"                  BIGINT       NOT NULL,
	CONSTRAINT "pk_jackett_sub_searches" PRIMARY KEY ("sub_search_id"),
	CONSTRAINT "fk_jackett_sub_searches_search_id" FOREIGN KEY ("search_id") REFERENCES "jackett_searches" ("search_id")
);
