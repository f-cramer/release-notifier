package de.cramer.releasenotifier.providers.bsto.entities

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

@Entity
@Table(name = "bsto_episodes")
class BsToEpisode(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "episode_id")
    var id: Long,

    @Column(name = "number")
    var number: Int,

    @Column(name = "name")
    var name: String,

    @ManyToOne
    @JoinColumn(name = "season_id")
    var season: BsToSeason,

    @OneToMany(mappedBy = "episode", cascade = [CascadeType.ALL])
    val links: MutableList<BsToLink>,
) {
    constructor(number: Int, name: String, season: BsToSeason) : this(0, number, name, season, mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BsToEpisode

        if (number != other.number) return false
        if (name != other.name) return false
        if (links != other.links) return false

        return true
    }

    override fun hashCode(): Int {
        var result = number
        result = 31 * result + name.hashCode()
        result = 31 * result + links.hashCode()
        return result
    }

    override fun toString(): String {
        return "Episode(id=$id, number=$number, name='$name')"
    }
}
