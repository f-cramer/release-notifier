package de.cramer.bstonotifier.repositories.bsto

import de.cramer.bstonotifier.entities.bsto.BsToSeries
import org.springframework.data.jpa.repository.JpaRepository

interface BsToSeriesRepository : JpaRepository<BsToSeries, Long>
