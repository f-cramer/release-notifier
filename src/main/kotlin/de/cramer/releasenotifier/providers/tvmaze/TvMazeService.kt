package de.cramer.releasenotifier.providers.tvmaze

import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeEpisode
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeIntegration
import de.cramer.releasenotifier.providers.tvmaze.entities.TvMazeNewEpisode
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
            var episodes = getEpisodes(show.id)
            if (lastCheckedDate != null) {
                episodes = episodes.filter { it.airstamp!!.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate() >= lastCheckedDate }
            }
            episodes
        }

        val today = LocalDate.now()
        integration.lastCheckedDate = today
        return episodes.asSequence()
            .sortedWith(compareBy<TvMazeEpisode> { it.season }.thenBy { it.number })
            .distinct()
            .filter { it.airstamp != null && it.airstamp.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate() <= today }
            .map { TvMazeNewEpisode(show.name, it.name, it.season, it.number, it.airstamp!!) }
            .toList()
    }

    private fun getShow(id: Long): TvMazeShow = processRequest { restTemplate.getForObject<TvMazeShow>("https://api.tvmaze.com/shows/$id") }

    private fun getEpisodes(showId: Long): List<TvMazeEpisode> = processRequest { restTemplate.exchange<List<TvMazeEpisode>>("https://api.tvmaze.com/shows/$showId/episodes", HttpMethod.GET).body!! }

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
