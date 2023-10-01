CREATE TABLE "tabletoptactics_configurations"
(
	"configuration_id" BIGSERIAL,
	"username"         VARCHAR(255) NOT NULL,
	"password"         VARCHAR(255) NOT NULL,
	CONSTRAINT "pk_tabletoptactics_configurations" PRIMARY KEY ("configuration_id")
);

CREATE TABLE "tabletoptactics_videos"
(
	"video_id"         BIGSERIAL    NOT NULL,
	"name"             VARCHAR(255) NOT NULL,
	"date"             DATE         NOT NULL,
	"url"              VARCHAR(255) NOT NULL,
	"thumbnail"        VARCHAR(255) NOT NULL,
	"configuration_id" BIGINT       NOT NULL,
	CONSTRAINT "pk_tabletoptactics_videos" PRIMARY KEY ("video_id"),
	CONSTRAINT "fk_tabletoptactics_videos_configuration_id" FOREIGN KEY ("configuration_id") REFERENCES "tabletoptactics_configurations" ("configuration_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);
