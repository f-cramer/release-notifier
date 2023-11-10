package de.cramer.releasenotifier.providers.jackett.specifications

import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class JackettSearchesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<JackettSearch> {
    override fun toPredicate(root: Root<JackettSearch>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(JackettSearch_.enabled), enabled)
    }
}
