package de.cramer.releasenotifier.providers.bsto

import de.cramer.releasenotifier.providers.bsto.entities.BsToEpisode
import de.cramer.releasenotifier.providers.bsto.entities.BsToLink
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.services.CheckerService
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.Locale

@Service
class BsToCheckerService(
    private val seriesRepository: BsToSeriesRepository,
    private val seriesService: BsToSeriesService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : CheckerService {
    @Transactional
    override fun check(): List<Message> {
        val allSeries: List<BsToSeries> = seriesRepository.findAll()
        val episodesBeforeUpdate = allSeries.allEpisodes

        allSeries.forEach { seriesService.update(it) }
        val episodesAfterUpdate = allSeries.allEpisodes

        val newEpisodes = (episodesAfterUpdate - episodesBeforeUpdate.toSet()).filter { it.links.isNotEmpty() }
        return newEpisodes.createMessages()
    }

    private fun List<BsToEpisode>.createMessages(): List<Message> {
        if (isEmpty()) {
            return emptyList()
        }

        return groupBy { it.season.series }.asSequence()
            .sortedWith(compareBy<Map.Entry<BsToSeries, List<BsToEpisode>>> { it.key.name }.thenBy { it.key.language })
            .map { (series, episodes) ->
                val sources = episodes.map { EpisodeSource(it) }
                val episodeString = if (episodes.size == 1) "episode" else "episodes"
                val subject = "${sources.size} new $episodeString available for series \"${series.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private val List<BsToSeries>.allEpisodes: List<BsToEpisode>
        get() = flatMap { it.seasons }.flatMap { it.episodes }

    private data class EpisodeSource(val episode: BsToEpisode) : HtmlMessageGenerator.Source<BsToContext> {
        private val links = episode.links.map { LinkSource(it) }

        override fun getText(context: BsToContext) =
            "${episode.season.number.format(context.seasonNumberLength)}.${episode.number.format(context.episodeNumberLength)} - ${episode.name}"

        override fun getUrl(context: BsToContext): URI? = null

        override fun getChildren(context: BsToContext) = links

        override fun generateContext(parentContext: BsToContext?) = parentContext ?: error("parent context has to exist for EpisodeSource")
    }

    private data class LinkSource(val link: BsToLink) : HtmlMessageGenerator.Source<BsToContext> {
        override fun getText(context: BsToContext) = link.hoster

        override fun getUrl(context: BsToContext) = link.url

        override fun getChildren(context: BsToContext) = emptyList<HtmlMessageGenerator.Source<BsToContext>>()

        override fun generateContext(parentContext: BsToContext?) = parentContext ?: error("parent context has to exist for LinkSource")
    }

    private data class BsToContext(
        val seasonNumberLength: Int,
        val episodeNumberLength: Int,
    )

    companion object {
        private fun Int.format(length: Int): String {
            return String.format(Locale.ROOT, "%0${length}d", this)
        }
    }
}
