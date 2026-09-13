package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.entities.ZBooleanEnabler
import de.cramer.releasenotifier.entities.ZDateBetweenEnabler
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.specifications.JackettSearchesWithEnabledTvMazeIntegrationSpecification
import de.cramer.releasenotifier.providers.tvmaze.TvMazeService
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration
import de.cramer.releasenotifier.utils.Message
import org.slf4j.Logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class JackettTvMazeEnablerService(
    private val searchRepository: JackettSearchRepository,
    private val tvMazeService: TvMazeService,
    private val eventPublisher: ApplicationEventPublisher,
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
            if (search.isDisabledExplicitly) {
                notify(
                    "TVMaze integration of search \"${search.name}\" has been disabled",
                    "The TVMaze integration of the already disabled search \"${search.name}\" has been disabled because its show ended at $showEndDate.",
                )
            } else {
                notify(
                    "Search \"${search.name}\" has been disabled",
                    "The search \"${search.name}\" and its TVMaze integration have been disabled because its show ended at $showEndDate.",
                )
                search.enabler = ZBooleanEnabler(false)
            }
            tvMazeIntegration.enabled = false
            return
        }

        // a disabled search is not checked, so its grace period would never be over
        // => enable it to catch up on the missed episodes, it will be disabled again once it has been checked
        if (showEndDate != null && search.isDisabledExplicitly) {
            notify(
                "Search \"${search.name}\" has been enabled",
                "The search \"${search.name}\" has been enabled to catch up on missed episodes because its show ended at $showEndDate. It will be disabled again once it has been checked.",
            )
            search.enabler = ZBooleanEnabler(true)
            return
        }

        val nextEpisodeAirDate = tvMazeService.getNextEpisodeAirDate(tvMazeIntegration)
        if (nextEpisodeAirDate != null) {
            // an already enabled search must not be touched, its start date would be moved to the next episode
            if (search.isDisabledExplicitly) {
                notify(
                    "Search \"${search.name}\" has been enabled",
                    "The search \"${search.name}\" has been enabled starting at $nextEpisodeAirDate because the air date of its next episode is known.",
                )
                search.enabler = ZDateBetweenEnabler(nextEpisodeAirDate, null)
            }
            return
        }

        if (search.isDisabledExplicitly) {
            return
        }

        val seasonEndDate = tvMazeService.getCurrentSeasonEndDate(tvMazeIntegration) ?: return
        if (seasonEndDate.isGracePeriodOver(tvMazeIntegration)) {
            notify(
                "Search \"${search.name}\" has been disabled",
                "The search \"${search.name}\" has been disabled because its current season ended at $seasonEndDate and the air date of its next episode is unknown.",
            )
            search.enabler = ZBooleanEnabler(false)
        }
    }

    private fun notify(subject: String, message: String) = eventPublisher.publishEvent(Message(subject, message, false))

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
