package de.cramer.releasenotifier.providers.tvmaze.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.Duration
import java.time.LocalDate

@Embeddable
class TvMazeIntegration(
    @Column(name = "tvmaze_show_id")
    var showId: Long,
    @Column(name = "tvmaze_last_checked")
    var lastCheckedDate: LocalDate?,
    @Column(name = "tvmaze_airstamp_offset")
    var airstampOffset: Duration?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TvMazeIntegration

        if (showId != other.showId) return false
        if (lastCheckedDate != other.lastCheckedDate) return false
        if (airstampOffset != other.airstampOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showId.hashCode()
        result = 31 * result + (lastCheckedDate?.hashCode() ?: 0)
        result = 31 * result + (airstampOffset?.hashCode() ?: 0)
        return result
    }
}
