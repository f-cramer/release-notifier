ALTER TABLE "jackett_sub_searches"
	DROP CONSTRAINT "fk_jackett_sub_searches_search_id";

ALTER TABLE "jackett_sub_searches"
	ADD CONSTRAINT "fk_jackett_sub_searches_search_id" FOREIGN KEY ("search_id") REFERENCES "jackett_searches" ("search_id")
		ON UPDATE CASCADE ON DELETE CASCADE;
