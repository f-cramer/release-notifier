package de.cramer.releasenotifier.providers.jackett.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.net.URI

@Entity
@Table(name = "jackett_searches")
class JackettSearch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @Column(name = "url")
    var url: URI,

    @Column(name = "name_prefix_pattern")
    var namePrefixPattern: String?,

    @Column(name = "name_suffix_pattern")
    var nameSuffixPattern: String?,

    @Column(name = "ignore_pattern")
    var ignorePattern: String?,

    var enabled: Boolean,

    @ElementCollection
    @CollectionTable(name = "jackett_searches_replacements", joinColumns = [JoinColumn(name = "search_id")])
    @MapKeyColumn(name = "pattern")
    @Column(name = "replacement")
    var replacements: MutableMap<String, String>,

    @OneToMany(mappedBy = "search", cascade = [CascadeType.ALL])
    var results: MutableList<JackettSearchResult>,
) {
    constructor(name: String, url: URI) : this(0, name, url, null, null, null, true, mutableMapOf(), mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JackettSearch

        if (name != other.name) return false
        if (url != other.url) return false
        if (namePrefixPattern != other.namePrefixPattern) return false
        if (nameSuffixPattern != other.nameSuffixPattern) return false
        if (ignorePattern != other.ignorePattern) return false
        if (replacements != other.replacements) return false
        if (results != other.results) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (namePrefixPattern?.hashCode() ?: 0)
        result = 31 * result + (nameSuffixPattern?.hashCode() ?: 0)
        result = 31 * result + (ignorePattern?.hashCode() ?: 0)
        result = 31 * result + replacements.hashCode()
        result = 31 * result + results.hashCode()
        return result
    }

    override fun toString(): String {
        return "JackettSearch(name='$name', url=$url, namePrefixPattern=$namePrefixPattern, nameSuffixPattern=$nameSuffixPattern, ignorePattern=$ignorePattern, replacements=$replacements)"
    }
}
