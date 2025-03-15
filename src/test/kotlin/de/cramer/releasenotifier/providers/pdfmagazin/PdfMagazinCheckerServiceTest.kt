package de.cramer.releasenotifier.providers.pdfmagazin

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
import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinIssue
import de.cramer.releasenotifier.providers.pdfmagazin.entities.PdfMagazinMagazine
import de.cramer.releasenotifier.services.HtmlMessageGenerator
import de.cramer.releasenotifier.utils.Message
import de.cramer.releasenotifier.utils.collection
import de.cramer.releasenotifier.utils.parse
import de.cramer.releasenotifier.utils.select
import de.cramer.releasenotifier.utils.uri
import net.datafaker.Faker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.lang.reflect.Method

@NotifierSpringBootTest
class PdfMagazinCheckerServiceTest {

    private val createMessage: Method = PdfMagazinCheckerService::class.java.getDeclaredMethod("createMessages", List::class.java)

    @MockitoBean
    private lateinit var repository: PdfMagazinMagazineRepository

    @MockitoBean
    private lateinit var magazineService: PdfMagazinMagazineService

    @Autowired
    private lateinit var htmlMessageGenerator: HtmlMessageGenerator

    private lateinit var checkerService: PdfMagazinCheckerService

    @BeforeEach
    fun setup() {
        checkerService = PdfMagazinCheckerService(repository, magazineService, htmlMessageGenerator)
    }

    @ParameterizedTest
    @MethodSource("generateTestInput")
    fun `multiple messages should be create for new issues of muliple magazines`(magazines: List<PdfMagazinMagazine>) {
        val newIssues = magazines.flatMap { it.issues }
        val messages = createMessages(newIssues)
        assertThat(messages).hasSameSizeAs(magazines)
        assertThat(messages).each { message ->
            message.prop(Message::html).isTrue()
            message.prop(Message::message).parse().all {
                // contains a number of top level div elements equal to the number of new issues for
                // because we do not which magazine this message was generated for, we only check for >= 1
                select("body > div").size().isGreaterThanOrEqualTo(1)

                // does not contain any unordered lists
                select("body > div > ul").isEmpty()

                // not an error message
                select("pre").isEmpty()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createMessages(newEpisodes: List<PdfMagazinIssue>) =
        createMessage.invoke(checkerService, newEpisodes) as List<Message>

    companion object {
        @JvmStatic
        private val faker = Faker()

        @JvmStatic
        @Suppress("UnusedPrivateMember")
        private fun generateTestInput(): List<List<PdfMagazinMagazine>> = generateSequence {
            faker.collection(1..5) { generateMagazine() }
        }.take(10).toList()

        @JvmStatic
        private fun generateMagazine(): PdfMagazinMagazine {
            val magazine = PdfMagazinMagazine(faker.science().scientist(), faker.internet().uri(), null)
            magazine.issues += faker.collection(1..5) { generateIssue(magazine) }
            return magazine
        }

        @JvmStatic
        private fun generateIssue(magazine: PdfMagazinMagazine): PdfMagazinIssue {
            return PdfMagazinIssue(faker.name().lastName(), faker.timeAndDate().birthday(), faker.internet().uri(), mutableMapOf(), magazine)
        }
    }
}
