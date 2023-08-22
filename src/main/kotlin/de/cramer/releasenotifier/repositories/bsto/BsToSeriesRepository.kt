package de.cramer.releasenotifier.repositories.bsto

import de.cramer.releasenotifier.entities.bsto.BsToSeries
import org.springframework.data.jpa.repository.JpaRepository

interface BsToSeriesRepository : JpaRepository<BsToSeries, Long>
