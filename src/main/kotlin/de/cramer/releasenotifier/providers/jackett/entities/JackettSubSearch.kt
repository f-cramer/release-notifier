package de.cramer.releasenotifier.providers.jackett.entities

import de.cramer.releasenotifier.entities.Enabler
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "jackett_sub_searches")
class JackettSubSearch(
    @Id
    @Column(name = "sub_search_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column(name = "season")
    var season: Int,

    @Column(name = "episode")
    var episode: Int,

    @Column(name = "url")
    var url: URI,

    @Embedded
    var enabler: Enabler,

    @ManyToOne
    @JoinColumn(name = "search_id")
    var search: JackettSearch,
) {
    constructor(season: Int, episode: Int, url: URI, enabler: Enabler, search: JackettSearch) : this(0, season, episode, url, enabler, search)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JackettSubSearch

        if (url != other.url) return false
        if (enabler != other.enabler) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + enabler.hashCode()
        return result
    }

    override fun toString(): String {
        return "JackettSubSearch(url=$url)"
    }
}
