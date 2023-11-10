package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface JackettSearchRepository : JpaRepository<JackettSearch, Long>, JpaSpecificationExecutor<JackettSearch>
