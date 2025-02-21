package de.cramer.releasenotifier.providers.downmagaz

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazIssue
import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import de.cramer.releasenotifier.providers.downmagaz.specifications.DownmagazMagazinesByEnabledSpecification
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.services.SimpleAbstractCheckerService
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service

@Service
class DownmagazCheckerService(
    private val magazineRepository: DownmagazMagazineRepository,
    private val magazineService: DownmagazMagazineService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : SimpleAbstractCheckerService<DownmagazMagazine, DownmagazIssue>() {
    override fun findAll(): List<DownmagazMagazine> = magazineRepository.findAll(DownmagazMagazinesByEnabledSpecification())

    override fun getChildren(t: DownmagazMagazine) = t.issues

    override fun update(t: DownmagazMagazine) = magazineService.update(t)

    override fun createMessages(newChildren: List<DownmagazIssue>): List<Message> {
        if (newChildren.isEmpty()) {
            return emptyList()
        }

        return newChildren.groupBy { it.magazine }.asSequence()
            .sortedBy { (k, _) -> k.name }
            .map { (magazine, issues) ->
                val sources = issues.map { IssueSource(it) }
                val issueString = if (issues.size == 1) "issue" else "issues"
                val subject = "${sources.size} new $issueString available for magazine \"${magazine.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private data class IssueSource(private val issue: DownmagazIssue) : HtmlMessageGenerator.Source<DownmagazContext> {
        override fun getText(context: DownmagazContext) = issue.name

        override fun getUrl(context: DownmagazContext) = issue.url

        override fun getChildren(context: DownmagazContext) = emptyList<HtmlMessageGenerator.Source<DownmagazContext>>()

        override fun generateContext(parentContext: DownmagazContext?) = DownmagazContext
    }

    private object DownmagazContext
}
