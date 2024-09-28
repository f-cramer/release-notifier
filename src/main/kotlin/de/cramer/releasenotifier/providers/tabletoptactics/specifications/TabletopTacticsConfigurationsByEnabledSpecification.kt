package de.cramer.releasenotifier.providers.tabletoptactics.specifications

import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsConfiguration
import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsConfiguration_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class TabletopTacticsConfigurationsByEnabledSpecification(
    private val enabled: Boolean = true,
) : Specification<TabletopTacticsConfiguration> {
    override fun toPredicate(root: Root<TabletopTacticsConfiguration>, query: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder): Predicate {
        return criteriaBuilder.equal(root.get(TabletopTacticsConfiguration_.enabled), enabled)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 9163095959074172459L
    }
}
