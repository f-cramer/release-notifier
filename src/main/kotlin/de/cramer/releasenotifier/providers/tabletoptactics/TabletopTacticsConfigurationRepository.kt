package de.cramer.releasenotifier.providers.tabletoptactics

import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsConfiguration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TabletopTacticsConfigurationRepository :
    JpaRepository<TabletopTacticsConfiguration, Long>,
    JpaSpecificationExecutor<TabletopTacticsConfiguration>
