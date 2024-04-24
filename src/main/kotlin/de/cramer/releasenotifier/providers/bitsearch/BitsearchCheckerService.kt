package de.cramer.releasenotifier.providers.bitsearch

import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchRelease
import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearch
import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearchResult
import de.cramer.releasenotifier.providers.bitsearch.specifications.BitsearchSearchesByEnabledSpecification
import de.cramer.releasenotifier.services.AbstractCheckerSerivce
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import java.net.URI

@Service
class BitsearchCheckerService(
    private val searchRepository: BitsearchSearchRepository,
    private val searchService: BitsearchSearchService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : AbstractCheckerSerivce<BitsearchSearch, BitsearchRelease>() {
    override fun findAll(): List<BitsearchSearch> = searchRepository.findAll(BitsearchSearchesByEnabledSpecification())

    override fun getChildren(t: BitsearchSearch) = t.results.flatMap { it.releases }

    override fun update(t: BitsearchSearch) = searchService.update(t)

    override fun createMessages(newChildren: List<BitsearchRelease>): List<Message> {
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

                val releaseString = if (releases.size == 1) "release" else "releases"
                val subject = "${sources.size} new $releaseString available for search \"${search.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private data class ResultSource(val result: BitsearchSearchResult, val releases: List<ReleaseSource>) : HtmlMessageGenerator.Source<BitsearchContext> {
        override fun getText(context: BitsearchContext) = result.name

        override fun getUrl(context: BitsearchContext) = null

        override fun getChildren(context: BitsearchContext) = releases

        override fun generateContext(parentContext: BitsearchContext?) = BitsearchContext
    }

    private data class ReleaseSource(val release: BitsearchRelease, val links: List<LinkSource>) : HtmlMessageGenerator.Source<BitsearchContext> {
        override fun getText(context: BitsearchContext) = release.title

        override fun getUrl(context: BitsearchContext) = null

        override fun getChildren(context: BitsearchContext) = links

        override fun generateContext(parentContext: BitsearchContext?) = BitsearchContext
    }

    private data class LinkSource(val link: URI) : HtmlMessageGenerator.Source<BitsearchContext> {
        override fun getText(context: BitsearchContext): String = link.toString()

        override fun getUrl(context: BitsearchContext) = link

        override fun getChildren(context: BitsearchContext) = emptyList<HtmlMessageGenerator.Source<BitsearchContext>>()

        override fun generateContext(parentContext: BitsearchContext?) = BitsearchContext
    }

    object BitsearchContext
}
