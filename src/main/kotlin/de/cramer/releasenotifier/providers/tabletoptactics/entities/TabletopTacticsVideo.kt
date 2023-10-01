package de.cramer.releasenotifier.providers.tabletoptactics.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.net.URI
import java.time.LocalDate

@Entity
@Table(name = "tabletoptactics_videos")
class TabletopTacticsVideo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @Column(name = "date")
    var date: LocalDate,

    @Column(name = "url")
    var url: URI,

    @Column(name = "thumbnail")
    var thumbnail: URI,

    @ManyToOne
    @JoinColumn(name = "configuration_id")
    var configuration: TabletopTacticsConfiguration,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabletopTacticsVideo

        if (name != other.name) return false
        if (url != other.url) return false
        if (thumbnail != other.thumbnail) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + thumbnail.hashCode()
        return result
    }
}
