CREATE TABLE "pdfmagazin_magazines"
(
	"magazine_id"                BIGSERIAL,
	"name"                       VARCHAR(255) NOT NULL,
	"url"                        VARCHAR(255) NOT NULL,
	"issue_name_prefix_pattern"  VARCHAR(255) NULL,
	"enabler_type"               VARCHAR(15)  NOT NULL DEFAULT 'boolean',
	"enabler_boolean_value"      BOOLEAN NULL DEFAULT true,
	"enabler_date_between_start" DATE NULL,
	"enabler_date_between_end"   DATE NULL,
	CONSTRAINT "pk_pdfmagazin_magazines" PRIMARY KEY ("magazine_id")
);

CREATE TABLE "pdfmagazin_issues"
(
	"issue_id"    BIGSERIAL,
	"name"        VARCHAR(255) NOT NULL,
	"date"        DATE         NOT NULL,
	"url"         VARCHAR(255) NOT NULL,
	"magazine_id" BIGINT       NOT NULL,
	CONSTRAINT "pk_pdfmagazin_issues" PRIMARY KEY ("issue_id"),
	CONSTRAINT "fk_pdfmagazin_issues_magazine_id" FOREIGN KEY ("magazine_id") REFERENCES "pdfmagazin_magazines" ("magazine_id")
		ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE "pdfmagazin_issues_links"
(
	"issue_id" BIGSERIAL,
	"name"     VARCHAR(255)  NOT NULL,
	"url"      VARCHAR(4096) NOT NULL,
	CONSTRAINT "pk_pdfmagazin_issues_links" PRIMARY KEY ("issue_id", "url"),
	CONSTRAINT "fk_pdfmagazin_issues_links_issue_id" FOREIGN KEY ("issue_id") REFERENCES "pdfmagazin_issues" ("issue_id")
		ON UPDATE CASCADE ON DELETE CASCADE
)
