package de.cramer.releasenotifier.providers.bsto.specifications

import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class BsToSeriesByNamesSpecification(
    private val names: Collection<String>,
) : Specification<BsToSeries> {
    override fun toPredicate(root: Root<BsToSeries>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder): Predicate {
        return if (names.isEmpty()) {
            criteriaBuilder.or()
        } else {
            root.get(BsToSeries_.name).`in`(names)
        }
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 905902466261340686L
    }
}
