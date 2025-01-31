package de.cramer.releasenotifier.providers.tvmaze.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate

@Embeddable
class TvMazeIntegration(
    @Column(name = "tvmaze_show_id")
    var showId: Long,
    @Column(name = "tvmaze_last_checked")
    var lastCheckedDate: LocalDate?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TvMazeIntegration

        if (showId != other.showId) return false
        if (lastCheckedDate != other.lastCheckedDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showId.hashCode()
        result = 31 * result + (lastCheckedDate?.hashCode() ?: 0)
        return result
    }
}
