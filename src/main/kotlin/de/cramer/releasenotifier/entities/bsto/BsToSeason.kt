package de.cramer.releasenotifier.entities.bsto

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "bsto_seasons")
class BsToSeason(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_id")
    var id: Long,

    @Column(name = "number")
    var number: Int,

    @Column(name = "url")
    var url: URI,

    @ManyToOne
    @JoinColumn(name = "series_id")
    var series: BsToSeries,

    @OneToMany(mappedBy = "season", cascade = [CascadeType.ALL])
    val episodes: MutableList<BsToEpisode>,
) {
    constructor(number: Int, url: URI, series: BsToSeries) : this(0, number, url, series, mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BsToSeason

        if (number != other.number) return false
        if (url != other.url) return false
        if (episodes != other.episodes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number
        result = 31 * result + url.hashCode()
        result = 31 * result + episodes.hashCode()
        return result
    }

    override fun toString(): String {
        return "Season(id=$id, number=$number', url=$url)"
    }
}
