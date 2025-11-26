package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface JackettReleaseRepository :
    JpaRepository<JackettRelease, Long>,
    JpaSpecificationExecutor<JackettRelease>
