package de.cramer.releasenotifier.providers.tvmaze.entities

import java.time.ZonedDateTime

data class TvMazeEpisode(
    val id: Long,
    val name: String,
    val season: Int,
    val number: Int,
    val airstamp: ZonedDateTime?,
)
