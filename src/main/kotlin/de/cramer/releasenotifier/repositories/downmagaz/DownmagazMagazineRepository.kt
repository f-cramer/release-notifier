package de.cramer.releasenotifier.repositories.downmagaz

import de.cramer.releasenotifier.entities.downmagaz.DownmagazMagazine
import org.springframework.data.jpa.repository.JpaRepository

interface DownmagazMagazineRepository : JpaRepository<DownmagazMagazine, Long>
