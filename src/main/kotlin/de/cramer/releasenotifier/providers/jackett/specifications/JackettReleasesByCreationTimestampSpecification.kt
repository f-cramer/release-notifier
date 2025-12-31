package de.cramer.releasenotifier.providers.jackett.specifications

import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial
import java.time.LocalDateTime

class JackettReleasesByCreationTimestampSpecification(
    private val minimumCreationTimestamp: LocalDateTime,
) : Specification<JackettRelease> {
    override fun toPredicate(root: Root<JackettRelease>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        return criteriaBuilder.greaterThanOrEqualTo(root.get(JackettRelease_.creationTimestamp), minimumCreationTimestamp)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -7557831259421830975L
    }
}
