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
    override fun check(): Message? {
        val allMagazines = magazineRepository.findAll()
        val issuesBeforeUpdate = allMagazines.allIssues

        allMagazines.forEach { magazineService.update(it) }
        val issuesAfterUpdate = allMagazines.allIssues

        val newIssues = issuesAfterUpdate - issuesBeforeUpdate.toSet()
        return newIssues.createMessage()
    }

    private fun List<DownmagazIssue>.createMessage(): Message? {
        if (isEmpty()) {
            return null
        }

        val sources = groupBy { it.magazine }.asSequence()
            .sortedBy { (k, _) -> k.name }
            .map { (magazine, issues) -> MagazineSource(magazine, issues.sortedBy { it.name }.map { IssueSource(it) }) }
            .toList()

        val magazinesText = if (sources.size == 1) "magazine" else "magazines"
        val header = "New issues available for ${sources.size} $magazinesText"

        return htmlMessageGenerator.generate(sources, "New issues", header)
    }

    private val List<DownmagazMagazine>.allIssues: List<DownmagazIssue>
        get() = flatMap { it.issues }

    private data class MagazineSource(val magazine: DownmagazMagazine, private val newIssues: List<IssueSource>) : HtmlMessageGenerator.Source<DownmagazContext> {
        override fun getText(context: DownmagazContext) = magazine.name

        override fun getUrl(context: DownmagazContext) = magazine.url

        override fun getChildren(context: DownmagazContext) = newIssues

        override fun generateContext(parentContext: DownmagazContext?) = DownmagazContext
    }

    private data class IssueSource(private val issue: DownmagazIssue) : HtmlMessageGenerator.Source<DownmagazContext> {
        override fun getText(context: DownmagazContext) = issue.name

        override fun getUrl(context: DownmagazContext) = issue.url

        override fun getChildren(context: DownmagazContext) = emptyList<HtmlMessageGenerator.Source<DownmagazContext>>()

        override fun generateContext(parentContext: DownmagazContext?) = DownmagazContext
    }

    private object DownmagazContext
}
