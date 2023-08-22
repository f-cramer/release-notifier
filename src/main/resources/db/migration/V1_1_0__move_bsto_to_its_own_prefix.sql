ALTER TABLE "series"
	RENAME TO "bsto_series";
ALTER TABLE "bsto_series"
	RENAME CONSTRAINT "pk_series" TO "pk_bsto_series";

ALTER TABLE "seasons"
	RENAME TO "bsto_seasons";
ALTER TABLE "bsto_seasons"
	RENAME CONSTRAINT "pk_seasons" TO "pk_bsto_seasons";
ALTER TABLE "bsto_seasons"
	RENAME CONSTRAINT "fk_seasons_series_id" TO "fk_bsto_seasons_series_id";

ALTER TABLE "episodes"
	RENAME TO "bsto_episodes";
ALTER TABLE "bsto_episodes"
	RENAME CONSTRAINT "pk_episodes" TO "pk_bsto_episodes";
ALTER TABLE "bsto_episodes"
	RENAME CONSTRAINT "fk_episodes_season_id" TO "fk_bsto_episodes_season_id";

ALTER TABLE "episode_links"
	RENAME TO "bsto_episode_links";
ALTER TABLE "bsto_episode_links"
	RENAME CONSTRAINT "pk_episode_links" TO "pk_bsto_episode_links";
ALTER TABLE "bsto_episode_links"
	RENAME CONSTRAINT "fk_episode_links_episode_id" TO "fk_bsto_episode_links_episode_id";
