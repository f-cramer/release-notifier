package de.cramer.bstonotifier.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "series")
class Series(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "series_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @Column(name = "language")
    var language: String,

    @Column(name = "url")
    var url: URI,

    @OneToMany(mappedBy = "series", cascade = [CascadeType.ALL])
    val seasons: MutableList<Season>,
) {
    constructor(url: String) : this(0, "", "", URI(url), mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Series

        if (name != other.name) return false
        if (language != other.language) return false
        if (url != other.url) return false
        if (seasons != other.seasons) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + seasons.hashCode()
        return result
    }

    override fun toString(): String {
        return "Series(id=$id, name='$name', language=$language, url=$url)"
    }
}
