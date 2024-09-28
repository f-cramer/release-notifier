package de.cramer.releasenotifier.providers.jackett.specifications

import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class JackettSearchesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<JackettSearch> {
    override fun toPredicate(root: Root<JackettSearch>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(JackettSearch_.enabled), enabled)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 8600029585211865773L
    }
}
