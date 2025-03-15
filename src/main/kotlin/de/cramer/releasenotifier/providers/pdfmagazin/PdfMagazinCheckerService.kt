package de.cramer.releasenotifier.providers.pdfmagazin

import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinIssue
import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinMagazine
import de.cramer.releasenotifier.providers.pdfmagazin.specifications.PdfMagazinMagazinesByEnabledSpecification
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.services.SimpleAbstractCheckerService
import de.cramer.releasenotifier.utils.Message
import org.springframework.stereotype.Service
import java.net.URI

@Service
class PdfMagazinCheckerService(
    private val magazineRepository: PdfMagazinMagazineRepository,
    private val magazineService: PdfMagazinMagazineService,
    private val htmlMessageGenerator: HtmlMessageGenerator,
) : SimpleAbstractCheckerService<PdfMagazinMagazine, PdfMagazinIssue>() {
    override fun findAll(): List<PdfMagazinMagazine> = magazineRepository.findAll(PdfMagazinMagazinesByEnabledSpecification())

    override fun getChildren(t: PdfMagazinMagazine) = t.issues

    override fun update(t: PdfMagazinMagazine) = magazineService.update(t)

    override fun createMessages(newChildren: List<PdfMagazinIssue>): List<Message> {
        if (newChildren.isEmpty()) {
            return emptyList()
        }

        return newChildren.groupBy { it.magazine }.asSequence()
            .sortedBy { (k, _) -> k.name }
            .map { (magazine, issues) ->
                val sources = issues.sortedByDescending { it.date }.map { IssueSource(it) }
                val issueString = if (issues.size == 1) "issue" else "issues"
                val subject = "${sources.size} new $issueString available for magazine \"${magazine.name}\""
                htmlMessageGenerator.generate(sources, subject, null)
            }
            .toList()
    }

    private data class IssueSource(private val issue: PdfMagazinIssue) : HtmlMessageGenerator.Source<PdfMagazinContext> {
        val links = issue.links.map { (name, url) -> LinkSource(name, url) }

        override fun getText(context: PdfMagazinContext) = issue.name

        override fun getUrl(context: PdfMagazinContext) = null

        override fun getChildren(context: PdfMagazinContext) = links

        override fun generateContext(parentContext: PdfMagazinContext?) = PdfMagazinContext
    }

    private data class LinkSource(private val name: String, private val url: URI) : HtmlMessageGenerator.Source<PdfMagazinContext> {
        override fun getText(context: PdfMagazinContext) = name

        override fun getUrl(context: PdfMagazinContext) = url

        override fun getChildren(context: PdfMagazinContext) = emptyList<HtmlMessageGenerator.Source<PdfMagazinContext>>()

        override fun generateContext(parentContext: PdfMagazinContext?) = PdfMagazinContext
    }

    private object PdfMagazinContext
}
