package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearchResult
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.springframework.stereotype.Service
import java.net.URI

@Service
class JackettSearchService(
    private val jsoupService: JsoupService,
) {
    fun update(search: JackettSearch) {
        val document = jsoupService.getDocument(search.url, parser = Parser.xmlParser())

        val ignore = search.ignorePattern?.toRegex(RegexOption.IGNORE_CASE)
        val namePrefix = search.namePrefixPattern?.toRegex(RegexOption.IGNORE_CASE)
        val nameSuffix = search.nameSuffixPattern?.toRegex(RegexOption.IGNORE_CASE)
        val replacements = search.replacements.mapKeys { (k, _) -> k.toRegex(RegexOption.IGNORE_CASE) }

        document.select("item").forEach { element ->
            search.addRelease(element, ignore, namePrefix, nameSuffix, replacements)
        }
    }

    private fun JackettSearch.addRelease(element: Element, ignore: Regex?, namePrefix: Regex?, nameSuffix: Regex?, replacements: Map<Regex, String>) {
        val title = element.selectFirst("title")!!.text()
        if (ignore != null && ignore.matches(title)) {
            return
        }

        val resultName = run {
            var t = title
            namePrefix?.matchAt(t, 0)?.let { t = t.substring(it.range.last + 1) }
            nameSuffix?.findAll(t)?.lastOrNull()?.takeIf { it.range.last == t.length - 1 }?.let { t = t.substring(0, it.range.first) }
            replacements.forEach { (regex, replacement) -> t = regex.replace(t, replacement) }
            t
        }

        val links = sequenceOf(
            element.select("guid").eachText(),
            element.select("link").eachText(),
            element.select("enclosure").eachAttr("url"),
            element.select("torznab|attr[name=magneturl]").eachAttr("value"),
        )
            .flatten()
            .map { runCatching { URI(it) } }
            .mapNotNull { it.getOrNull() }
            .filterNot { it.scheme == url.scheme && it.host == url.host && it.port == url.port }
            .toSet()

        val existingResult = results.find { it.name == resultName }
        val result = existingResult ?: JackettSearchResult(resultName, this)

        val releaseTitle = title.replace(' ', '.')
        val existingRelease = result.releases.find { it.title == releaseTitle }
        val release = existingRelease ?: run {
            val release = JackettRelease(releaseTitle, result)
            result.releases += release
            release
        }

        release.links += links
        if (existingResult == null && links.isNotEmpty()) {
            results += result
        }
    }
}
