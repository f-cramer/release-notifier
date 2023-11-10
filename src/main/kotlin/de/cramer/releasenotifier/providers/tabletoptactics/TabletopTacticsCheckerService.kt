package de.cramer.releasenotifier.providers.tabletoptactics

import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsConfiguration
import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsVideo
import de.cramer.releasenotifier.providers.tabletoptactics.specifications.TabletopTacticsConfigurationsByEnabledSpecification
import de.cramer.releasenotifier.services.AbstractCheckerSerivce
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import java.net.URI

@Service
class TabletopTacticsCheckerService(
    private val configurationRepository: TabletopTacticsConfigurationRepository,
    private val configurationService: TabletopTacticsConfigurationService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : AbstractCheckerSerivce<TabletopTacticsConfiguration, TabletopTacticsVideo>() {
    override fun findAll(): List<TabletopTacticsConfiguration> = configurationRepository.findAll(TabletopTacticsConfigurationsByEnabledSpecification())

    override fun getChildren(t: TabletopTacticsConfiguration) = t.videos

    override fun update(t: TabletopTacticsConfiguration) = configurationService.update(t)

    override fun createMessages(newChildren: List<TabletopTacticsVideo>): List<Message> {
        if (newChildren.isEmpty()) {
            return emptyList()
        }

        return newChildren.groupBy { it.configuration }.asSequence()
            .sortedBy { (k, _) -> k.username }
            .map { (_, videos) ->
                val sources = videos.map { VideoSource(it) }

                val videoString = if (sources.size == 1) "video" else "videos"
                val subject = "${sources.size} new $videoString avaiable from TabletopTactics"
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private data class VideoSource(val video: TabletopTacticsVideo) : HtmlMessageGenerator.Source<TabletopTacticsContext> {
        override fun getText(context: TabletopTacticsContext) = "${video.date} - ${video.name}"

        override fun getUrl(context: TabletopTacticsContext) = null

        override fun getChildren(context: TabletopTacticsContext) = listOf(
            LinkSource("Thumbnail", video.thumbnail),
            LinkSource("Video", video.url),
        )

        override fun generateContext(parentContext: TabletopTacticsContext?) = TabletopTacticsContext
    }

    private data class LinkSource(val text: String, val url: URI) : HtmlMessageGenerator.Source<TabletopTacticsContext> {
        override fun getText(context: TabletopTacticsContext) = text

        override fun getUrl(context: TabletopTacticsContext) = url

        override fun getChildren(context: TabletopTacticsContext) = emptyList<HtmlMessageGenerator.Source<TabletopTacticsContext>>()

        override fun generateContext(parentContext: TabletopTacticsContext?) = TabletopTacticsContext
    }

    private object TabletopTacticsContext
}
