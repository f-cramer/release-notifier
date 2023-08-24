package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearchResult
import de.cramer.releasenotifier.services.CheckerService
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class JackettCheckerService(
    private val searchRepository: JackettSearchRepository,
    private val searchService: JackettSearchService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : CheckerService {
    @Transactional
    override fun check(): List<Message> {
        val allSearches = searchRepository.findAll()
        val releasesBeforeUpdate = allSearches.allReleases

        allSearches.forEach { searchService.update(it) }
        val releasesAfterUpdate = allSearches.allReleases

        val newReleases = releasesAfterUpdate - releasesBeforeUpdate.toSet()
        return newReleases.createMessages()
    }

    private fun List<JackettRelease>.createMessages(): List<Message> {
        if (isEmpty()) {
            return emptyList()
        }

        return groupBy { it.result.search }.asSequence()
            .sortedBy { (k, _) -> k.name }
            .map { (search, releases) ->
                val sources = releases.groupBy { it.result }.asSequence()
                    .sortedBy { (k, _) -> k.name }
                    .map { (result, releases) ->
                        ResultSource(result, releases.map { r -> ReleaseSource(r, r.links.map { LinkSource(it) }) })
                    }
                    .toList()

                val releaseString = if (releases.size == 1) "release" else "releases"
                val subject = "${sources.size} new $releaseString available for search \"${search.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private val List<JackettSearch>.allReleases: List<JackettRelease>
        get() = flatMap { it.results }.flatMap { it.releases }

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
