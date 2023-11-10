package de.cramer.releasenotifier.providers.bsto.specifications

import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class BsToSeriesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<BsToSeries> {
    override fun toPredicate(root: Root<BsToSeries>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(BsToSeries_.enabled), enabled)
    }
}
