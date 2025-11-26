package de.cramer.releasenotifier.providers.bsto

import de.cramer.releasenotifier.providers.bsto.entities.BsToEpisode
import de.cramer.releasenotifier.providers.bsto.entities.BsToLink
import de.cramer.releasenotifier.providers.bsto.entities.BsToSeries
import de.cramer.releasenotifier.providers.bsto.specifications.BsToSeriesByEnabledSpecification
import de.cramer.releasenotifier.providers.bsto.specifications.BsToSeriesByNamesSpecification
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.services.SimpleAbstractCheckerService
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import java.net.URI
import java.util.Locale

@Service
class BsToCheckerService(
    private val seriesRepository: BsToSeriesRepository,
    private val seriesService: BsToSeriesService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : SimpleAbstractCheckerService<BsToSeries, BsToEpisode>() {
    override fun findAll(): List<BsToSeries> = seriesRepository.findAll(BsToSeriesByEnabledSpecification())

    override fun getChildren(t: BsToSeries) = t.seasons.flatMap { it.episodes }

    override fun update(t: BsToSeries) = seriesService.update(t)

    override fun createMessages(newChildren: List<BsToEpisode>): List<Message> {
        if (newChildren.isEmpty()) {
            return emptyList()
        }

        val newEpisodesBySeries = newChildren.groupBy { it.season.series }
        val allSeries = seriesRepository.findAll(BsToSeriesByNamesSpecification(newEpisodesBySeries.keys.map { it.name }))
        return newEpisodesBySeries.asSequence()
            .sortedWith(EPISODES_BY_SERIES_COMPARATOR)
            .map { (series, episodes) ->
                val context = BsToContext(
                    series.seasons.maxOf { it.number }.toString().length,
                    series.seasons.flatMap { it.episodes }.maxOf { it.number }.toString().length,
                )
                val sources = episodes.map { EpisodeSource(it, context) }
                val episodeString = if (episodes.size == 1) "episode" else "episodes"
                val seriesWithSameNameExists = allSeries.any { it.id != series.id && it.name == series.name }
                val seriesName = "${series.name} ${if (seriesWithSameNameExists) "(${series.language.uppercase()})" else ""}".trim()
                val subject = "${sources.size} new $episodeString available for series \"${seriesName}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private data class EpisodeSource(val episode: BsToEpisode, val context: BsToContext) : HtmlMessageGenerator.Source<BsToContext> {
        private val links = episode.links.map { LinkSource(it) }

        override fun getText(context: BsToContext) = "${episode.season.number.format(context.seasonNumberLength)}.${episode.number.format(context.episodeNumberLength)} - ${episode.name}"

        override fun getUrl(context: BsToContext): URI? = null

        override fun getChildren(context: BsToContext) = links

        override fun generateContext(parentContext: BsToContext?) = context
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
        private val EPISODES_BY_SERIES_COMPARATOR = compareBy<Map.Entry<BsToSeries, List<BsToEpisode>>> { it.key.name }.thenBy { it.key.language }
        private fun Int.format(length: Int): String {
            return String.format(Locale.ROOT, "%0${length}d", this)
        }
    }
}
