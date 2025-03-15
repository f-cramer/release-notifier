package de.cramer.releasenotifier.providers.pdfmagazin

import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinIssue
import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinMagazine
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class PdfMagazinMagazineService(
    private val jsoupService: JsoupService,
) {
    fun update(magazine: PdfMagazinMagazine) {
        val document = magazine.url.getDocument() ?: return
        val items = document.select(".mag_item")
        items.forEach { magazine.addIssue(it) }
    }

    private fun PdfMagazinMagazine.addIssue(issueElement: Element) {
        val issueNamePrefix = issueNamePrefixPattern?.toRegex(RegexOption.IGNORE_CASE)
        val linkElement = issueElement.selectFirst("a:not(:has(> *))")
        val url = linkElement?.attr("href")?.let { URI(it) } ?: return
        val name = linkElement.text().getIssueName(issueNamePrefix) ?: return
        val date = issueElement.select("p strong").eachText()
            .firstNotNullOfOrNull { runCatching { LocalDate.parse(it, DATE_FORMATTER) }.getOrNull() }
            ?: error("could not find date for issue $name at $url")

        if (issues.any { it.name == name && it.date == date }) {
            return
        }

        val document = url.getDocument() ?: return

        val links = document.select(".mag_details a.dw").associate {
            val hoster = it.select(".download strong").text()
            val links = URI(it.attr("href"))
            hoster to links
        }.toMutableMap()

        issues += PdfMagazinIssue(name, date, url, links, this)
    }

    private fun String.getIssueName(namePrefix: Regex?): String? {
        var t = this
        if (namePrefix != null) {
            namePrefix.matchAt(t, 0)?.let { t = t.substring(it.range.last + 1) } ?: return null
        }
        return t.trim()
    }

    private fun URI.getDocument(): Document? {
        val (document, statusCode) = try {
            jsoupService.getDocument(this, JSOUP_CONFIGURATION_KEY, ignoreHttpErrors = true)
        } catch (_: SocketTimeoutException) {
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

    companion object {
        private const val JSOUP_CONFIGURATION_KEY = "pdfmagazin"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
        private val IGNORED_STATUS_CODES = emptySet<Int>()
    }
}
