package de.cramer.releasenotifier.providers.bitsearch.entities

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
@Table(name = "bitsearch_search_results")
class BitsearchSearchResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @ManyToOne
    @JoinColumn(name = "search_id")
    var search: BitsearchSearch,

    @OneToMany(mappedBy = "result", cascade = [CascadeType.ALL])
    var releases: MutableList<BitsearchRelease>,
) {
    constructor(name: String, search: BitsearchSearch) : this(0, name, search, mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitsearchSearchResult

        if (name != other.name) return false
        if (releases != other.releases) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + releases.hashCode()
        return result
    }

    override fun toString(): String {
        return "BitsearchSearchResult(id=$id, name='$name')"
    }
}
