package de.cramer.releasenotifier.providers.bsto

import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface BsToSeriesRepository :
    JpaRepository<BsToSeries, Long>,
    JpaSpecificationExecutor<BsToSeries>
