CREATE TABLE "jackett_searches"
(
	"search_id"           BIGSERIAL,
	"name"                VARCHAR(255) NOT NULL,
	"url"                 VARCHAR(255) NOT NULL,
	"name_prefix_pattern" VARCHAR(255) NULL,
	"name_suffix_pattern" VARCHAR(255) NULL,
	"ignore_pattern"      VARCHAR(255) NULL,
	CONSTRAINT "pk_jackett_searches" PRIMARY KEY ("search_id")
);

CREATE TABLE "jackett_searches_replacements"
(
	"search_id"   BIGINT       NOT NULL,
	"pattern"     VARCHAR(255) NOT NULL,
	"replacement" VARCHAR(255) NOT NULL,
	CONSTRAINT "pk_jackett_searches_replacements" PRIMARY KEY ("search_id", "pattern", "replacement"),
	CONSTRAINT "fk_jackett_searches_replacements_search_id" FOREIGN KEY ("search_id") REFERENCES "jackett_searches" ("search_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE "jackett_search_results"
(
	"result_id" BIGSERIAL,
	"name"      VARCHAR(255) NOT NULL,
	"search_id" BIGINT       NOT NULL,
	CONSTRAINT "pk_jackett_search_results" PRIMARY KEY ("result_id"),
	CONSTRAINT "fk_jackett_search_results_search_id" FOREIGN KEY ("search_id") REFERENCES "jackett_searches" ("search_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE "jackett_releases"
(
	"release_id" BIGSERIAL,
	"title"      VARCHAR(255) NOT NULL,
	"result_id"  BIGINT       NOT NULL,
	CONSTRAINT "pk_jackett_releases" PRIMARY KEY ("release_id"),
	CONSTRAINT "fk_jackett_releases_result_id" FOREIGN KEY ("result_id") REFERENCES "jackett_search_results" ("result_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE "jackett_releases_links"
(
	"release_id" BIGSERIAL,
	"url"        VARCHAR(4096) NOT NULL,
	CONSTRAINT "pk_jackett_releases_links" PRIMARY KEY ("release_id", "url"),
	CONSTRAINT "fk_jackett_releases_links_release_id" FOREIGN KEY ("release_id") REFERENCES "jackett_releases" ("release_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);
