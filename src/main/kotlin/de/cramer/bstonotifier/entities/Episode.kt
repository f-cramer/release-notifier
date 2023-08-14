package de.cramer.bstonotifier.entities

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
@Table(name = "episodes")
class Episode(
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
    var season: Season,

    @OneToMany(mappedBy = "episode", cascade = [CascadeType.ALL])
    val links: MutableList<Link>,
) {
    constructor(number: Int, name: String, season: Season) : this(0, number, name, season, mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Episode

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
