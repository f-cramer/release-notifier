package de.cramer.bstonotifier.services.bsto

import de.cramer.bstonotifier.entities.bsto.BsToEpisode
import de.cramer.bstonotifier.entities.bsto.BsToSeries
import de.cramer.bstonotifier.repositories.bsto.BsToSeriesRepository
import de.cramer.bstonotifier.services.CheckerService
import de.cramer.bstonotifier.utils.Message
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.stream.appendHTML
import kotlinx.html.ul
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.StringWriter

@Service
class BsToCheckerService(
    private val seriesRepository: BsToSeriesRepository,
    private val seriesService: BsToSeriesService,
) : CheckerService {
    @Transactional
    override fun check(): Message? {
        val allSeries: List<BsToSeries> = seriesRepository.findAll()
        val episodesBeforeUpdate = allSeries.allEpisodes

        allSeries.forEach { seriesService.update(it) }
        val episodesAfterUpdate = allSeries.allEpisodes

        val newEpisodes = (episodesAfterUpdate - episodesBeforeUpdate.toSet()).filter { it.links.isNotEmpty() }
        return newEpisodes.createMessage()
    }

    private fun List<BsToEpisode>.createMessage(): Message? {
        if (isEmpty()) {
            return null
        }

        val episodesBySeries = groupBy { it.season.series }.toList()
            .sortedWith(compareBy<Pair<BsToSeries, List<BsToEpisode>>> { it.first.name }.thenBy { it.first.language })
        val writer = StringWriter()
        writer.appendHTML().html {
            body {
                val seriesText = if (episodesBySeries.size == 1) "series" else "series'"
                h3 { text("New Episodes available for ${episodesBySeries.size} $seriesText") }
                episodesBySeries.forEach { (series, episodes) ->
                    val seasonNumberLength = series.seasons.maxOf { it.number }.toString().length
                    val episodeNumberLength = series.seasons.flatMap { it.episodes }.maxOf { it.number }.toString().length

                    div {
                        h4 { a(href = series.url.toString()) { text("${series.name} (${series.language})") } }
                        ul {
                            episodes.forEach { episode ->
                                li {
                                    text("${episode.season.number.format(seasonNumberLength)}.${episode.number.format(episodeNumberLength)} - ${episode.name}")
                                    ul {
                                        episode.links.forEach { link ->
                                            li {
                                                a(href = link.url.toString()) { text(link.hoster) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Message(writer.toString(), true)
    }

    private fun Int.format(length: Int): String {
        return String.format("%0${length}d", this)
    }

    private val List<BsToSeries>.allEpisodes: List<BsToEpisode>
        get() = flatMap { it.seasons }.flatMap { it.episodes }
}
