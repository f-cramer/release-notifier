package de.cramer.releasenotifier.providers.bitsearch.entities

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "bitsearch_releases")
class BitsearchRelease(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "release_id")
    var id: Long,

    @Column(name = "title")
    var title: String,

    @ElementCollection
    @CollectionTable(name = "bitsearch_releases_links", joinColumns = [JoinColumn(name = "release_id")])
    @Column(name = "url")
    var links: MutableSet<URI>,

    @ManyToOne
    @JoinColumn(name = "result_id")
    var result: BitsearchSearchResult,
) {
    constructor(title: String, result: BitsearchSearchResult) : this(0, title, mutableSetOf(), result)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitsearchRelease

        if (title != other.title) return false
        if (links != other.links) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + links.hashCode()
        return result
    }

    override fun toString(): String {
        return "BitsearchRelease(id=$id, title='$title', links=$links)"
    }
}
