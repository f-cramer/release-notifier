package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearchResult
import de.cramer.releasenotifier.providers.jackett.specifications.JackettReleasesByCreationTimestampSpecification
import de.cramer.releasenotifier.providers.jackett.specifications.JackettSearchesByEnabledSpecification
import de.cramer.releasenotifier.services.AbstractCheckerService
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import java.net.URI
import java.time.LocalDateTime

@Service
class JackettCheckerService(
    private val searchRepository: JackettSearchRepository,
    private val searchService: JackettSearchService,
    private val releaseRepository: JackettReleaseRepository,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : AbstractCheckerService<JackettSearch, JackettRelease>() {
    override fun findAll(): List<JackettSearch> = searchRepository.findAll(JackettSearchesByEnabledSpecification())

    override fun initializeState(elements: List<JackettSearch>): State<JackettSearch, JackettRelease> = JackettState()

    override fun update(t: JackettSearch) = searchService.update(t)

    override fun createMessages(newChildren: List<JackettRelease>): List<Message> {
        if (newChildren.isEmpty()) {
            return emptyList()
        }

        return newChildren.groupBy { it.result.search }.asSequence()
            .sortedBy { (k, _) -> k.name }
            .map { (search, releases) ->
                val sources = releases.groupBy { it.result }.asSequence()
                    .sortedBy { (k, _) -> k.name }
                    .map { (result, releases) ->
                        ResultSource(result, releases.map { r -> ReleaseSource(r, r.links.map { LinkSource(it) }) })
                    }
                    .toList()

                val releaseString = if (sources.size == 1) "release" else "releases"
                val subject = "${sources.size} new $releaseString available for search \"${search.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private inner class JackettState : State<JackettSearch, JackettRelease> {
        private val created: LocalDateTime = LocalDateTime.now()

        override fun getNewChildren(elements: List<JackettSearch>): List<JackettRelease> = releaseRepository.findAll(JackettReleasesByCreationTimestampSpecification(created))
    }

    private data class ResultSource(val result: JackettSearchResult, val releases: List<ReleaseSource>) : HtmlMessageGenerator.Source<JackettContext> {
        override fun getText(context: JackettContext) = result.name

        override fun getUrl(context: JackettContext) = null

        override fun getChildren(context: JackettContext) = releases

        override fun generateContext(parentContext: JackettContext?) = JackettContext
    }

    private data class ReleaseSource(val release: JackettRelease, val links: List<LinkSource>) : HtmlMessageGenerator.Source<JackettContext> {
        override fun getText(context: JackettContext) = release.title

        override fun getUrl(context: JackettContext) = null

        override fun getChildren(context: JackettContext) = links

        override fun generateContext(parentContext: JackettContext?) = JackettContext
    }

    private data class LinkSource(val link: URI) : HtmlMessageGenerator.Source<JackettContext> {
        override fun getText(context: JackettContext): String = link.toString()

        override fun getUrl(context: JackettContext) = link

        override fun getChildren(context: JackettContext) = emptyList<HtmlMessageGenerator.Source<JackettContext>>()

        override fun generateContext(parentContext: JackettContext?) = JackettContext
    }

    object JackettContext
}
