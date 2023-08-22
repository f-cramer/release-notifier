CREATE TABLE "downmagaz_magazines"
(
	"magazine_id" BIGSERIAL,
	"name"        VARCHAR(255) NOT NULL,
	"url"         VARCHAR(255) NOT NULL,
	CONSTRAINT "pk_downmagaz_magazines" PRIMARY KEY ("magazine_id")
);

CREATE TABLE "downmagaz_issues"
(
	"issue_id"    BIGSERIAL,
	"name"        VARCHAR(255) NOT NULL,
	"url"         VARCHAR(255) NOT NULL,
	"magazine_id" BIGINT       NOT NULL,
	CONSTRAINT "pk_downmagaz_issues" PRIMARY KEY ("issue_id"),
	CONSTRAINT "fk_downmagaz_issues_magazine_id" FOREIGN KEY ("magazine_id") REFERENCES "downmagaz_magazines" ("magazine_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);
