package de.cramer.releasenotifier.providers.bsto.specifications

import de.cramer.releasenotifier.entities.Enabler
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class BsToSeriesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<BsToSeries> {
    override fun toPredicate(root: Root<BsToSeries>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder): Predicate {
        return Enabler.getPredicate(enabled, root.join(BsToSeries_.enabler), criteriaBuilder)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4071917664633932109L
    }
}
