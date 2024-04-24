package de.cramer.releasenotifier.providers.jackett

import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.hasSameSizeAs
import assertk.assertions.isEmpty
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.size
import de.cramer.releasenotifier.NotifierSpringBootTest
import de.cramer.releasenotifier.providers.jackett.entities.JackettRelease
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearch
import de.cramer.releasenotifier.providers.jackett.entities.JackettSearchResult
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import de.cramer.releasenotifier.utils.collection
import de.cramer.releasenotifier.utils.select
import de.cramer.releasenotifier.utils.uri
import net.datafaker.Faker
import org.jsoup.Jsoup
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.lang.reflect.Method

@NotifierSpringBootTest
class JackettCheckerServiceTest {

    private val createMessage: Method = JackettCheckerService::class.java.getDeclaredMethod("createMessages", List::class.java)

    @MockBean
    private lateinit var repository: JackettSearchRepository

    @MockBean
    private lateinit var seriesService: JackettSearchService

    @Autowired
    private lateinit var htmlMessageGenerator: HtmlMessageGenerator

    private lateinit var checkerService: JackettCheckerService

    @BeforeEach
    fun setup() {
        checkerService = JackettCheckerService(repository, seriesService, htmlMessageGenerator)
    }

    @ParameterizedTest
    @MethodSource("generateTestInput")
    fun `multiple messages should be create for new releases of muliple series`(searches: List<JackettSearch>) {
        val newReleases = searches.flatMap { it.results }.flatMap { it.releases }
        val messages = createMessages(newReleases)
        assertThat(messages).hasSameSizeAs(searches)
        assertThat(messages).each { message ->
            message.prop(Message::html).isTrue()
            message.prop(Message::message).transform { Jsoup.parse(it) }.all {
                // contains a number of top level div elements equal to the number of seasons that contain new releases
                // because we do not which season this message was generated for, we only check for >= 1
                select("body > div").size().isGreaterThanOrEqualTo(1)

                // contains exactly one unordered list for each top level div element
                // because we do not which season this message was generated for, we only check for >= 1
                select("body > div > ul").size().isGreaterThanOrEqualTo(1)

                // not an error message
                select("pre").isEmpty()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createMessages(newReleases: List<JackettRelease>) =
        createMessage.invoke(checkerService, newReleases) as List<Message>

    companion object {
        @JvmStatic
        private val faker = Faker()

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun generateTestInput(): List<List<JackettSearch>> = generateSequence {
            faker.collection(1..5) { generateSearch() }
        }.take(10).toList()

        @JvmStatic
        private fun generateSearch(): JackettSearch {
            val search = JackettSearch(faker.show().play(), faker.internet().uri())
            search.results += faker.collection(1..5) { generateResult(search) }
            return search
        }

        @JvmStatic
        private fun generateResult(search: JackettSearch): JackettSearchResult {
            val season = JackettSearchResult(faker.funnyName().name(), search)
            season.releases += faker.collection(5..15) { generateRelease(season) }
            return season
        }

        @JvmStatic
        private fun generateRelease(result: JackettSearchResult): JackettRelease {
            val release = JackettRelease(faker.funnyName().name(), result)
            release.links += faker.collection(1..4) { faker.internet().uri() }
            return release
        }
    }
}
