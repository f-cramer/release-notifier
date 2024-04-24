package de.cramer.releasenotifier.providers.bitsearch.specifications

import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearch
import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearch_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class BitsearchSearchesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<BitsearchSearch> {
    override fun toPredicate(root: Root<BitsearchSearch>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(BitsearchSearch_.enabled), enabled)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 8600029585211865773L
    }
}
