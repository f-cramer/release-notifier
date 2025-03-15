package de.cramer.releasenotifier.providers.pdfmagazin.entities

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import java.net.URI
import java.time.LocalDate

@Entity
@Table(name = "pdfmagazin_issues")
class PdfMagazinIssue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @Column(name = "date")
    var date: LocalDate,

    @Column(name = "url")
    var url: URI,

    @ElementCollection
    @CollectionTable(name = "pdfmagazin_issues_links", joinColumns = [JoinColumn(name = "issue_id")])
    @MapKeyColumn(name = "name")
    @Column(name = "url")
    var links: MutableMap<String, URI>,

    @ManyToOne
    @JoinColumn(name = "magazine_id")
    var magazine: PdfMagazinMagazine,
) {
    constructor(name: String, date: LocalDate, url: URI, links: MutableMap<String, URI>, magazine: PdfMagazinMagazine) : this(0, name, date, url, links, magazine)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PdfMagazinIssue

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
        return "PdfMagazinIssue(id=$id, name=$name, url=$url)"
    }
}
