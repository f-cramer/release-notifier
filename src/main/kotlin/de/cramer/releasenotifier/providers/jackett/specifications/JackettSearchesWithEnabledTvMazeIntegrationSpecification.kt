package de.cramer.releasenotifier.providers.jackett.specifications

import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch_
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration_
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.io.Serial

class JackettSearchesWithEnabledTvMazeIntegrationSpecification : Specification<JackettSearch> {
    override fun toPredicate(root: Root<JackettSearch>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate {
        val tvMazeIntegration = root.get(JackettSearch_.tvMazeIntegration)
        return criteriaBuilder.and(
            criteriaBuilder.isNotNull(tvMazeIntegration.get(TvMazeIntegration_.showId)),
            criteriaBuilder.isTrue(tvMazeIntegration.get(TvMazeIntegration_.enabled)),
        )
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -5822170384962150173L
    }
}
