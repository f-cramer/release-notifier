package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.entities.ZBooleanEnabler
import de.cramer.releasenotifier.entities.ZDateBetweenEnabler
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.specifications.JackettSearchesWithEnabledTvMazeIntegrationSpecification
import de.cramer.releasenotifier.providers.tvmaze.TvMazeService
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration
import org.slf4j.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class JackettTvMazeEnablerService(
    private val searchRepository: JackettSearchRepository,
    private val tvMazeService: TvMazeService,
    private val log: Logger,
) {
    @Transactional
    @Scheduled(cron = $$"${jackett.tvmaze.enabler-update.schedule:0 0 4 * * *}")
    fun updateEnablers() {
        searchRepository.findAll(JackettSearchesWithEnabledTvMazeIntegrationSpecification())
            .forEach {
                try {
                    updateEnabler(it)
                } catch (e: Exception) {
                    log.error("error while updating enabler of search \"${it.name}\"", e)
                }
            }
    }

    private fun updateEnabler(search: JackettSearch) {
        val tvMazeIntegration = search.tvMazeIntegration ?: return

        val showEndDate = tvMazeService.getShowEndDate(tvMazeIntegration)
        if (showEndDate != null && showEndDate.isGracePeriodOver(tvMazeIntegration)) {
            log.debug("disabling search \"{}\" and its tvmaze integration because its show ended at {}", search.name, showEndDate)
            tvMazeIntegration.enabled = false
            search.enabler = ZBooleanEnabler(false)
            return
        }

        val nextEpisodeAirDate = tvMazeService.getNextEpisodeAirDate(tvMazeIntegration)
        if (nextEpisodeAirDate != null) {
            // an already enabled search must not be touched, its start date would be moved to the next episode
            if (search.isDisabledExplicitly) {
                log.debug("enabling search \"{}\" starting at {} because the air date of its next episode is known", search.name, nextEpisodeAirDate)
                search.enabler = ZDateBetweenEnabler(nextEpisodeAirDate, null)
            }
            return
        }

        if (search.isDisabledExplicitly) {
            return
        }

        val seasonEndDate = tvMazeService.getCurrentSeasonEndDate(tvMazeIntegration) ?: return
        if (seasonEndDate.isGracePeriodOver(tvMazeIntegration)) {
            log.debug("disabling search \"{}\" because its current season ended at {} and the air date of its next episode is unknown", search.name, seasonEndDate)
            search.enabler = ZBooleanEnabler(false)
        }
    }

    private val JackettSearch.isDisabledExplicitly: Boolean
        get() = enabler.let { it is ZBooleanEnabler && !it.enabled }

    private fun LocalDate.isGracePeriodOver(integration: TvMazeIntegration): Boolean {
        val lastCheckedDate = integration.lastCheckedDate ?: return false
        return plusDays(GRACE_PERIOD_IN_DAYS) < lastCheckedDate
    }

    companion object {
        private const val GRACE_PERIOD_IN_DAYS = 2L
    }
}
