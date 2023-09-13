package de.cramer.releasenotifier.providers.downmagaz

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazIssue
import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.net.URI

@Service
class DownmagazMagazineService(
    private val jsoupService: JsoupService,
    private val log: Logger,
) {
    fun update(magazine: DownmagazMagazine) {
        val (document, _) = jsoupService.getDocument(magazine.url, JSOUP_CONFIGURATION_KEY)
        val pages = document.select(".catPages a")

        if (pages.isEmpty()) {
            log.trace("{}", document)
            error("could not find page information for magazine at ${magazine.url}")
        }
        val pageCount = pages[pages.size - 2].text().toInt()

        val documents = sequenceOf(document) + generateSequence(2) { it + 1 }.takeWhile { it <= pageCount }
            .map { magazine.url + "page/$it/" }
            .map { jsoupService.getDocument(it, JSOUP_CONFIGURATION_KEY).document }
        documents.forEach { magazine.addIssues(it) }
    }

    private fun DownmagazMagazine.addIssues(document: Document) {
        document.select(".fstory .stitle a").forEach {
            val name = it.text()
            if (issues.none { i -> i.name == name }) {
                issues += DownmagazIssue(name, URI(it.attr("abs:href")), this)
            }
        }
    }

    operator fun URI.plus(suffix: String): URI {
        val path = rawPath
        val separator = if (path.endsWith("/")) "" else "/"
        val newPath = path + separator + suffix
        return resolve(newPath)
    }

    companion object {
        private const val JSOUP_CONFIGURATION_KEY = "downmagaz"
    }
}
