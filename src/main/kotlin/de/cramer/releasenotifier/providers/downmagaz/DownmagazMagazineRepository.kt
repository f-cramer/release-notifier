package de.cramer.releasenotifier.providers.downmagaz

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import org.springframework.data.jpa.repository.JpaRepository

interface DownmagazMagazineRepository : JpaRepository<DownmagazMagazine, Long>
