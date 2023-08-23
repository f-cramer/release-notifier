package de.cramer.releasenotifier.providers.downmagaz.entities

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
@Table(name = "downmagaz_issues")
class DownmagazIssue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @Column(name = "url")
    var url: URI,

    @ManyToOne
    @JoinColumn(name = "magazine_id")
    var magazine: DownmagazMagazine,
) {
    constructor(name: String, url: URI, magazine: DownmagazMagazine) : this(0, name, url, magazine)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownmagazIssue

        if (name != other.name) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }

    override fun toString(): String {
        return "DownmagazIssue(id=$id, name=$name, url=$url)"
    }
}
