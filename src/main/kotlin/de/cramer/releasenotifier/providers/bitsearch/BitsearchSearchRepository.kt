package de.cramer.releasenotifier.providers.bitsearch

import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface BitsearchSearchRepository : JpaRepository<BitsearchSearch, Long>, JpaSpecificationExecutor<BitsearchSearch>
