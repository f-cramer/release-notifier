package de.cramer.releasenotifier.providers.tvmaze.entities

import java.time.LocalDate

data class TvMazeSeason(
    val id: Long,
    val number: Int,
    val premiereDate: LocalDate?,
    val endDate: LocalDate?,
)
