package de.cramer.releasenotifier.providers.downmagaz

import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazIssue
import de.cramer.releasenotifier.providers.downmagaz.entities.DownmagazMagazine
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpTimeoutException

@Service
class DownmagazMagazineService(
    private val jsoupService: JsoupService,
    private val log: Logger,
) {
    fun update(magazine: DownmagazMagazine) {
        val document = magazine.url.getDocument() ?: return
        val pages = document.select(".catPages a")

        if (pages.isEmpty()) {
            log.trace("{}", document)
            error("could not find page information for magazine at ${magazine.url}")
        }
        val pageCount = pages[pages.size - 2].text().toInt()

        val documents = sequenceOf(document) + (2..pageCount).asSequence()
            .map { magazine.url + "page/$it/" }
            .mapNotNull { it.getDocument() }
        documents.forEach { magazine.addIssues(it) }
    }

    private fun DownmagazMagazine.addIssues(document: Document) {
        document.select(".fstory .stitle a").forEach {
            val name = it.text()
            val url = URI(it.attr("abs:href"))
            if (issues.none { i -> i.name == name || i.url == url }) {
                issues += DownmagazIssue(name, url, this)
            }
        }
    }

    private fun URI.getDocument(): Document? {
        val (document, statusCode) = try {
            jsoupService.getDocument(this, JSOUP_CONFIGURATION_KEY, ignoreHttpErrors = true)
        } catch (_: SocketTimeoutException) {
            return null
        } catch (_: HttpTimeoutException) {
            return null
        } catch (e: IOException) {
            if (e.message == "Underlying input stream returned zero bytes") {
                return null
            } else {
                throw e
            }
        }

        if (statusCode in IGNORED_STATUS_CODES) {
            return null
        } else {
            val statusCodeClass = statusCode / 100
            @Suppress("MagicNumber")
            if (statusCodeClass == 4 || statusCodeClass == 5) {
                error("received status code $statusCode when loading $this")
            }
        }

        return document
    }

    operator fun URI.plus(suffix: String): URI {
        val path = rawPath
        val separator = if (path.endsWith("/")) "" else "/"
        val newPath = path + separator + suffix
        return resolve(newPath)
    }

    companion object {
        private const val JSOUP_CONFIGURATION_KEY = "downmagaz"
        private val IGNORED_STATUS_CODES = setOf(500, 503, 521)
    }
}
