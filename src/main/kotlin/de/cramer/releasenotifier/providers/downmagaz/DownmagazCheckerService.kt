package de.cramer.releasenotifier.providers.downmagaz

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazIssue
import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import de.cramer.releasenotifier.services.CheckerService
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DownmagazCheckerService(
    private val magazineRepository: DownmagazMagazineRepository,
    private val magazineService: DownmagazMagazineService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : CheckerService {
    @Transactional
    override fun check(): List<Message> {
        val allMagazines = magazineRepository.findAll()
        val issuesBeforeUpdate = allMagazines.allIssues

        allMagazines.forEach { magazineService.update(it) }
        val issuesAfterUpdate = allMagazines.allIssues

        val newIssues = issuesAfterUpdate - issuesBeforeUpdate.toSet()
        return newIssues.createMessages()
    }

    private fun List<DownmagazIssue>.createMessages(): List<Message> {
        if (isEmpty()) {
            return emptyList()
        }

        return groupBy { it.magazine }.asSequence()
            .sortedBy { (k, _) -> k.name }
            .map { (magazine, issues) ->
                val sources = issues.map { IssueSource(it) }
                val issueString = if (issues.size == 1) "issue" else "issues"
                val subject = "${sources.size} new $issueString available for magazine \"${magazine.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private val List<DownmagazMagazine>.allIssues: List<DownmagazIssue>
        get() = flatMap { it.issues }

    private data class IssueSource(private val issue: DownmagazIssue) : HtmlMessageGenerator.Source<DownmagazContext> {
        override fun getText(context: DownmagazContext) = issue.name

        override fun getUrl(context: DownmagazContext) = issue.url

        override fun getChildren(context: DownmagazContext) = emptyList<HtmlMessageGenerator.Source<DownmagazContext>>()

        override fun generateContext(parentContext: DownmagazContext?) = DownmagazContext
    }

    private object DownmagazContext
}
