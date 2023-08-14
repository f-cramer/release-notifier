CREATE TABLE "series"
(
	"series_id" BIGSERIAL,
	"name"      VARCHAR(255) NOT NULL,
	"url"       VARCHAR(255) NOT NULL,
	CONSTRAINT "pk_series" PRIMARY KEY ("series_id")
);

CREATE TABLE "seasons"
(
	"season_id" BIGSERIAL,
	"number"    INT          NOT NULL,
	"language"  VARCHAR(10)  NOT NULL,
	"url"       VARCHAR(255) NOT NULL,
	"series_id" BIGINT       NOT NULL,
	CONSTRAINT "pk_seasons" PRIMARY KEY ("season_id"),
	CONSTRAINT "fk_seasons_series_id" FOREIGN KEY ("series_id") REFERENCES "series" ("series_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE "episodes"
(
	"episode_id" BIGSERIAL,
	"number"     INT          NOT NULL,
	"name"       VARCHAR(255) NOT NULL,
	"season_id"  BIGINT       NOT NULL,
	CONSTRAINT "pk_episodes" PRIMARY KEY ("episode_id"),
	CONSTRAINT "fk_episodes_season_id" FOREIGN KEY ("season_id") REFERENCES "seasons" ("season_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE "episode_links"
(
	"episode_link_id" BIGSERIAL,
	"hoster"          VARCHAR(255) NOT NULL,
	"url"             VARCHAR(255) NOT NULL,
	"episode_id"      BIGINT       NOT NULL,
	CONSTRAINT "pk_episode_links" PRIMARY KEY ("episode_link_id"),
	CONSTRAINT "fk_episode_links_episode_id" FOREIGN KEY ("episode_id") REFERENCES "episodes" ("episode_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);
