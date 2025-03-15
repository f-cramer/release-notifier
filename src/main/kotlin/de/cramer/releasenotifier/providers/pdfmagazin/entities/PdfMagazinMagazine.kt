package de.cramer.releasenotifier.providers.pdfmagazin.entities

import de.cramer.releasenotifier.entities.Enabler
import de.cramer.releasenotifier.entities.ZBooleanEnabler
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
@Table(name = "pdfmagazin_magazines")
class PdfMagazinMagazine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "magazine_id")
    var id: Long,

    @Column(name = "name")
    var name: String,

    @Column(name = "url")
    var url: URI,

    @Column(name = "issue_name_prefix_pattern")
    var issueNamePrefixPattern: String?,

    var enabler: Enabler,

    @OneToMany(mappedBy = "magazine", cascade = [CascadeType.ALL])
    val issues: MutableList<PdfMagazinIssue>,
) {
    constructor(name: String, url: URI, issueTitlePattern: String?) : this(0, name, url, issueTitlePattern, ZBooleanEnabler.TRUE, mutableListOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PdfMagazinMagazine

        if (name != other.name) return false
        if (url != other.url) return false
        if (issues != other.issues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + issues.hashCode()
        return result
    }

    override fun toString(): String {
        return "PdfMagazinMagazine(id=$id, name='$name', url=$url)"
    }
}
