package de.cramer.releasenotifier.providers.tvmaze.entities

import java.time.ZonedDateTime

data class TvMazeNewEpisode(
    val show: String,
    val name: String,
    val season: Int,
    val episode: Int,
    val airstamp: ZonedDateTime,
)
