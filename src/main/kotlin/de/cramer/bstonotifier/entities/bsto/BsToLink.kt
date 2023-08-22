package de.cramer.bstonotifier.entities.bsto

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "bsto_episode_links")
data class BsToLink(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "episode_link_id")
    var id: Long,

    @Column(name = "hoster")
    var hoster: String,

    @Column(name = "url")
    var url: URI,

    @JoinColumn(name = "episode_id")
    @ManyToOne
    var episode: BsToEpisode,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BsToLink

        if (hoster != other.hoster) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hoster.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }

    override fun toString(): String {
        return "Link(id=$id, hoster='$hoster', url=$url)"
    }
}
