package de.cramer.releasenotifier.providers.tvmaze.entities

import java.time.LocalDate

data class TvMazeShow(
    val id: Long,
    val name: String,
    val premiered: LocalDate?,
    val ended: LocalDate?,
)
