package de.cramer.releasenotifier.providers.bsto

import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import org.springframework.data.jpa.repository.JpaRepository

interface BsToSeriesRepository : JpaRepository<BsToSeries, Long>
