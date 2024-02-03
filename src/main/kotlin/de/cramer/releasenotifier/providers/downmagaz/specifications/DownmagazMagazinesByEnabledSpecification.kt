package de.cramer.releasenotifier.providers.downmagaz.specifications

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class DownmagazMagazinesByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<DownmagazMagazine> {
    override fun toPredicate(root: Root<DownmagazMagazine>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(DownmagazMagazine_.enabled), enabled)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -1371181923720518353L
    }
}
