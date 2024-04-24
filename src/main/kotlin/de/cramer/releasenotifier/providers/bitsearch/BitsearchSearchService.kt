package de.cramer.releasenotifier.providers.bitsearch

import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchRelease
import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearch
import de.cramer.releasenotifier.providers.bitsearch.entities.BitsearchSearchResult
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.SocketTimeoutException
import java.net.URI

@Service
class BitsearchSearchService(
    private val jsoupService: JsoupService,
) {
    fun update(search: BitsearchSearch) {
        val url = UriComponentsBuilder.fromUriString("https://bitsearch.to/search")
            .queryParam("q", search.query.replace(" ", "+"))
            .queryParam("sort", "date")
            .build().toUri()
        val (document, _) = try {
            jsoupService.getDocument(url, JSOUP_CONFIGURATION_KEY)
        } catch (_: SocketTimeoutException) {
            return
        }

        val ignore = search.ignorePattern?.toRegex(RegexOption.IGNORE_CASE)
        val namePrefix = search.namePrefixPattern?.toRegex(RegexOption.IGNORE_CASE)
        val nameSuffix = search.nameSuffixPattern?.toRegex(RegexOption.IGNORE_CASE)
        val replacements = search.replacements.mapKeys { (k, _) -> k.toRegex(RegexOption.IGNORE_CASE) }

        document.select(".card.search-result").forEach { element ->
            search.addRelease(element, ignore, namePrefix, nameSuffix, replacements)
        }
    }

    private fun BitsearchSearch.addRelease(element: Element, ignore: Regex?, namePrefix: Regex?, nameSuffix: Regex?, replacements: Map<Regex, String>) {
        val title = replacements.entries.fold(element.selectFirst(".title")?.text().orEmpty()) { t, (k, v) -> t.replace(k, v) }
        if (ignore != null && ignore.matches(title)) {
            return
        }

        val resultName = title.getResultName(namePrefix, nameSuffix, replacements) ?: return
        val links = (
            sequenceOf(element.selectFirst(".title a")?.attr("abs:href")) +
                element.select(".links a").eachAttr("abs:href")
            )
            .filterNotNull()
            .map { runCatching { URI(it) } }
            .mapNotNull { it.getOrNull() }
            .toSet()

        val existingResult = results.find { it.name.equals(resultName, ignoreCase = true) }
        val result = existingResult ?: BitsearchSearchResult(resultName, this)

        val releaseTitle = title.replace(' ', '.').trim()
        val existingRelease = result.releases.find { it.title.equals(releaseTitle, ignoreCase = true) }
        val release = existingRelease ?: run {
            val release = BitsearchRelease(releaseTitle, result)
            result.releases += release
            release
        }

        release.links += links
        if (existingResult == null && links.isNotEmpty()) {
            results += result
        }
    }

    private fun String.getResultName(namePrefix: Regex?, nameSuffix: Regex?, replacements: Map<Regex, String>): String? {
        var t = this
        if (namePrefix != null) {
            namePrefix.matchAt(t, 0)?.let { t = t.substring(it.range.last + 1) } ?: return null
        }
        if (nameSuffix != null) {
            nameSuffix.findAll(t).lastOrNull()?.takeIf { it.range.last == t.length - 1 }?.let { t = t.substring(0, it.range.first) } ?: return null
        }
        replacements.forEach { (regex, replacement) -> t = regex.replace(t, replacement) }
        return t.trim()
    }

    companion object {
        private const val JSOUP_CONFIGURATION_KEY = "bitsearch"
    }
}
