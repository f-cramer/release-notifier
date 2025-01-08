package de.cramer.releasenotifier.providers.tabletoptactics.entities

import de.cramer.releasenotifier.entities.Enabler
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "tabletoptactics_configurations")
class TabletopTacticsConfiguration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "configuration_id")
    var id: Long,

    @Column(name = "username")
    var username: String,

    @Column(name = "password")
    var password: String,

    var enabler: Enabler,

    @OneToMany(mappedBy = "configuration", cascade = [CascadeType.ALL])
    var videos: MutableList<TabletopTacticsVideo>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabletopTacticsConfiguration

        if (username != other.username) return false
        if (password != other.password) return false
        if (videos != other.videos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + videos.hashCode()
        return result
    }
}
