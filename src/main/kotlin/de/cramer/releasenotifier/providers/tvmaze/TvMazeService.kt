package de.cramer.releasenotifier.providers.tvmaze

import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeEpisode
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeNewEpisode
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeSeason
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeShow
import de.cramer.releasenotifier.utils.TimedLock
import org.slf4j.Logger
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.getForObject
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

private const val WAITING_TIME_AFTER_TOO_MANY_REQUESTS = 2000L

@Service
class TvMazeService(
    private val restTemplate: RestTemplate,
    private val log: Logger,
) {
    @Suppress("MagicNumber")
    val requestLock = TimedLock("tvmaze", Duration.ofMillis(500), log)

    fun getNewEpisodes(integration: TvMazeIntegration): List<TvMazeNewEpisode> {
        val show = getShow(integration.showId)
        val lastCheckedDate = integration.lastCheckedDate

        val episodes = run {
            val episodes = getRegularEpisodes(show.id, integration.airstampOffset)
            if (lastCheckedDate != null) {
                episodes.filter { it.airDate >= lastCheckedDate }
            } else {
                episodes
            }
        }

        val today = LocalDate.now()
        integration.lastCheckedDate = today
        return episodes.asSequence()
            .sortedWith(compareBy<TvMazeEpisode> { it.season }.thenBy { it.number })
            .distinct()
            .filter { it.airDate <= today }
            .map { TvMazeNewEpisode(show.name, it.name, it.season, it.number!!, it.airstamp!!) }
            .toList()
    }

    private val TvMazeEpisode.airDate: LocalDate
        get() = airstamp!!.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

    private fun getShow(id: Long): TvMazeShow = processRequest { restTemplate.getForObject<TvMazeShow>("https://api.tvmaze.com/shows/$id") ?: error("show with id $id not found") }

    private fun getSeasons(showId: Long): List<TvMazeSeason> = processRequest { restTemplate.exchange<List<TvMazeSeason>>("https://api.tvmaze.com/shows/$showId/seasons", HttpMethod.GET).body!! }

    private fun getEpisodes(showId: Long): List<TvMazeEpisode> = processRequest { restTemplate.exchange<List<TvMazeEpisode>>("https://api.tvmaze.com/shows/$showId/episodes?specials=1", HttpMethod.GET).body!! }

    fun getShowEndDate(integration: TvMazeIntegration): LocalDate? = getShow(integration.showId).ended

    fun getCurrentSeasonEndDate(integration: TvMazeIntegration): LocalDate? {
        val today = LocalDate.now()
        val endDate = getSeasons(integration.showId).asSequence()
            .filter { it.premiereDate != null && it.premiereDate <= today }
            .maxByOrNull { it.number }
            ?.endDate ?: return null
        val airstampOffset = integration.airstampOffset ?: return endDate
        return endDate.plusDays(airstampOffset.toDays())
    }

    fun getNextEpisodeAirDate(integration: TvMazeIntegration): LocalDate? {
        val today = LocalDate.now()
        return getRegularEpisodes(integration.showId, integration.airstampOffset).asSequence()
            .map { it.airDate }
            .filter { it >= today }
            .minOrNull()
    }

    private fun getRegularEpisodes(showId: Long, airstampOffset: Duration?): List<TvMazeEpisode> {
        val episodes = getEpisodes(showId).filter { it.airstamp != null }
            .filter { it.number != null } // number == null => special episode
        return if (airstampOffset != null) {
            episodes.map { it.copy(airstamp = it.airstamp!!.plus(airstampOffset)) }
        } else {
            episodes
        }
    }

    private fun <T> processRequest(request: () -> T): T = requestLock.withLock {
        var response: T? = null
        while (response == null) {
            try {
                response = request()
            } catch (_: HttpClientErrorException.TooManyRequests) {
                log.warn("too many requests, waiting for 2 seconds")
                Thread.sleep(WAITING_TIME_AFTER_TOO_MANY_REQUESTS)
            }
        }
        response
    }
}
