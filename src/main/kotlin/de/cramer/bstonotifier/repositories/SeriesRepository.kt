package de.cramer.bstonotifier.repositories

import de.cramer.bstonotifier.entities.Series
import org.springframework.data.jpa.repository.JpaRepository

interface SeriesRepository : JpaRepository<Series, Long>
