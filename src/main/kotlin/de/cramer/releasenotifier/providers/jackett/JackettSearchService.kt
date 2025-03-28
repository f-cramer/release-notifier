package de.cramer.releasenotifier.providers.jackett

import de.cramer.releasenotifier.entities.ZDateBetweenEnabler
import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearchResult
import de.cramer.releasenotifier.providers.jackett.entities.JackettSubSearch
import de.cramer.releasenotifier.providers.tvmaze.TvMazeService
import de.cramer.releasenotifier.services.JsoupService
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils
import java.net.SocketTimeoutException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@Service
class JackettSearchService(
    private val jsoupService: JsoupService,
    private val tvMazeService: TvMazeService,
    private val log: Logger,
) {
    private var latestExceptions: MutableMap<Long, Throwable> = mutableMapOf()

    fun update(search: JackettSearch) {
        try {
            doUpdate(search)
            latestExceptions -= search.id
        } catch (e: Exception) {
            latestExceptions[search.id]?.let {
                if (it::class.java == e::class.java) {
                    throw e
                }
            }

            latestExceptions[search.id] = e
        }
    }

    private fun doUpdate(search: JackettSearch) {
        val tvMazeIntegration = search.tvMazeIntegration
        if (tvMazeIntegration != null) {
            search.subSearches += tvMazeService.getNewEpisodes(tvMazeIntegration).mapNotNull {
                val show = UriComponentsBuilder.fromUri(search.url).build(true).queryParams.getFirst("q")?.let { q -> UriUtils.decode(q, StandardCharsets.UTF_8) } ?: it.show
                val request = String.format(Locale.ROOT, "%s S%02dE%02d", show, it.season, it.episode)
                val uri = UriComponentsBuilder.fromUri(search.url).replaceQueryParam("q", UriUtils.encodeQueryParam(request, StandardCharsets.UTF_8)).build(true).toUri()
                val airDate = it.airstamp.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                val enablerEnd = maxOf(airDate.plusDays(2), LocalDate.now())
                val enabler = ZDateBetweenEnabler(airDate, enablerEnd)

                val existingSubSearch = search.subSearches.find { ss -> ss.season == it.season && ss.episode == it.episode }
                if (existingSubSearch != null) {
                    existingSubSearch.url = uri
                    existingSubSearch.enabler = enabler
                    null
                } else {
                    JackettSubSearch(it.season, it.episode, uri, enabler, search)
                }
            }
        }

        if (search.subSearches.isEmpty()) {
            doUpdate(search, search.url)
        } else {
            search.subSearches.asSequence()
                .filter { it.enabler.enabled }
                .forEach { subSearch -> doUpdate(subSearch.search, subSearch.url) }
        }
    }

    private fun doUpdate(search: JackettSearch, uri: URI) {
        val (document, statusCode) = try {
            jsoupService.getDocument(uri, JSOUP_CONFIGURATION_KEY, timeout = Duration.ofMinutes(2), ignoreHttpErrors = true)
        } catch (_: SocketTimeoutException) {
            return
        }

        val rootElement = document.selectFirst(":root")!!
        if (rootElement.tagName() == "error") {
            val code = rootElement.attr("code")
            val description = rootElement.attr("description")
            if (IGNORED_ERROR_DESCRIPTIONS.any { it in description }) {
                return
            }
            error("error while executing request for search \"${search.name}\" (code: $code, description: $description)")
        }

        @Suppress("MagicNumber")
        if (statusCode >= 400) {
            log.error("{}", document)
            error("unknown error while executing request for search \"${search.name}\" (code: $statusCode)")
        }

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

        val resultName = title.getResultName(namePrefix, nameSuffix, replacements) ?: return
        val links = sequenceOf(
            element.select("comments").eachText(),
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

        val existingResult = results.find { it.name.equals(resultName, ignoreCase = true) }
        val result = existingResult ?: JackettSearchResult(resultName, this)

        val releaseTitle = title.replace(' ', '.').trim()
        val existingRelease = result.releases.find { it.title.equals(releaseTitle, ignoreCase = true) }
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
        private const val JSOUP_CONFIGURATION_KEY = "jackett"
        private val IGNORED_ERROR_DESCRIPTIONS = setOf(
            "The tracker seems to be down.",
        )
    }
}
