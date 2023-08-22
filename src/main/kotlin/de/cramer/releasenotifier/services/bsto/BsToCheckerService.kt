package de.cramer.releasenotifier.services.bsto

import de.cramer.releasenotifier.entities.bsto.BsToEpisode
import de.cramer.releasenotifier.entities.bsto.BsToLink
import de.cramer.releasenotifier.entities.bsto.BsToSeries
import de.cramer.releasenotifier.repositories.bsto.BsToSeriesRepository
import de.cramer.releasenotifier.services.CheckerService
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class BsToCheckerService(
    private val seriesRepository: BsToSeriesRepository,
    private val seriesService: BsToSeriesService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
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

        val sources = groupBy { it.season.series }.toList()
            .sortedWith(compareBy<Pair<BsToSeries, List<BsToEpisode>>> { it.first.name }.thenBy { it.first.language })
            .map { (series, episodes) -> SeriesSource(series, episodes.map { EpisodeSource(it) }) }
        val seriesText = if (sources.size == 1) "series" else "series'"
        val header = "New Episodes available for ${sources.size} $seriesText"

        return htmlMessageGenerator.generate(sources, "New episodes", header)
    }

    private val List<BsToSeries>.allEpisodes: List<BsToEpisode>
        get() = flatMap { it.seasons }.flatMap { it.episodes }

    private data class SeriesSource(val series: BsToSeries, val newEpisodes: List<EpisodeSource>) : HtmlMessageGenerator.Source<BsToContext> {
        override fun getText(context: BsToContext) = "${series.name} (${series.language})"

        override fun getUrl(context: BsToContext) = series.url

        override fun getChildren(context: BsToContext) = newEpisodes

        override fun generateContext(parentContext: BsToContext?) = BsToContext(
            series.seasons.maxOf { it.number }.toString().length,
            series.seasons.flatMap { it.episodes }.maxOf { it.number }.toString().length,
        )
    }

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
}

private fun Int.format(length: Int): String {
    return String.format("%0${length}d", this)
}
