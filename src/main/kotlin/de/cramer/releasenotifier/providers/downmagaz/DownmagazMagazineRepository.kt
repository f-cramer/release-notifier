package de.cramer.releasenotifier.providers.downmagaz

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface DownmagazMagazineRepository : JpaRepository<DownmagazMagazine, Long>, JpaSpecificationExecutor<DownmagazMagazine>
