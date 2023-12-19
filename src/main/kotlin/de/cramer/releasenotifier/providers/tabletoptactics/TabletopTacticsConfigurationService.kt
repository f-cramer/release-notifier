package de.cramer.releasenotifier.providers.tabletoptactics

import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsConfiguration
import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsVideo
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.WindowType
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

@Service
class TabletopTacticsConfigurationService(
    @Value("\${selenium.firefox.binary-path:#{null}}") private val firefoxBinaryPath: String?,
    private val log: Logger,
) {
    fun update(configuration: TabletopTacticsConfiguration) {
        val options = FirefoxOptions().apply {
            firefoxBinaryPath?.let { setBinary(it) }
            addArguments("-headless")
        }
        val driver = FirefoxDriver(options)
        try {
            val wait = WebDriverWait(driver, Duration.ofMinutes(1))

            driver.get("https://tabletoptactics.tv/log-in/")
            @Suppress("MagicNumber")
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("form[name=login] input[type=hidden]"), 3))

            driver.findElement(By.id("cn-accept-cookie")).click()

            driver.findElement(By.id("user_login")).sendKeys(configuration.username)
            val passwordField = driver.findElement(By.id("user_pass"))
            passwordField.sendKeys(configuration.password)
            passwordField.submit()

            wait.until(elementToBeClickable(By.name("avatar_submit")))
            driver.findElement(By.linkText("SHOWS")).click()

            val videosSelector = By.cssSelector(".type-post.status-publish")
            wait.until(elementToBeClickable(videosSelector))

            val videoElements = driver.findElements(videosSelector).filterNotNull()
            videoElements.forEach {
                driver.updateVideo(it, wait, configuration)
            }
        } catch (e: Exception) {
            val now = LocalDateTime.now().format(FILE_DATE_FORMATTER)
            val screenshot = driver.getFullPageScreenshotAs(OutputType.FILE)
            screenshot.copyTo(File("tabletop-tactics-$now-screenshot.png"))
            screenshot.delete()
            val pageSourceFile = File("tabletop-tactics-$now-page-source.html")
            pageSourceFile.writeText(driver.pageSource)
            throw e
        } finally {
            driver.quit()
        }
    }

    private fun FirefoxDriver.updateVideo(element: WebElement, wait: WebDriverWait, configuration: TabletopTacticsConfiguration) {
        val details = element.findElement(By.className("qt-details"))
        val dateText = Jsoup.parse(details.getAttribute("outerHTML").trim())
            .select("body > ${details.tagName}").textNodes()
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "")
        val date = LocalDate.parse(dateText.trim(), DATE_FORMATTER)
        val name = element.findElement(By.cssSelector(".qt-title a")).text.trim()
        val background = try {
            // it can happen that videos do not contain a background right after being uploaded
            element.findElement(By.className("qt-header-bg"))
        } catch (e: Exception) {
            log.info(e.message, e)
            return
        }
        val imageUrl = URI.create(background.getAttribute("data-bgimage"))

        val originalTab = windowHandle

        // load video page url in new tab
        val videoPageUrl = background.getAttribute("href")
        switchTo().newWindow(WindowType.TAB)

        try {
            try {
                // loading errors, it will work next time
                get(videoPageUrl)
            } catch (e: TimeoutException) {
                log.trace(e.message, e)
                return
            }

            wait.until(elementToBeClickable(By.id("qtcontents")))

            val videoUrl = getYoutubeUrl()
                ?: getTTUrl(wait)

            val existingVideo = configuration.videos.find {
                (it.name.equals(name, ignoreCase = true) || it.url == videoUrl) && it.date == date
            }
            if (existingVideo == null) {
                val video = TabletopTacticsVideo(0, name, date, videoUrl, imageUrl, configuration)
                configuration.videos += video
            } else {
                existingVideo.name = name
                existingVideo.url = videoUrl
                existingVideo.thumbnail = imageUrl
            }
        } finally {
            close()
            switchTo().window(originalTab)
        }
    }

    private fun WebDriver.getYoutubeUrl(): URI? {
        val uri = findElements(By.className("perfmatters-lazy-youtube"))
            .firstOrNull()
            ?.getAttribute("data-src")
            ?.runCatching(URI::create)
            ?.getOrNull()
        return uri ?: getYoutubeFallbackUrl()
    }

    private fun WebDriver.getYoutubeFallbackUrl(): URI? {
        return findElements(By.cssSelector("#qtcontents .qt-the-content > p"))
            .firstOrNull()
            ?.text
            ?.runCatching(URI::create)
            ?.getOrNull()
    }

    private fun RemoteWebDriver.getTTUrl(wait: Wait<WebDriver>): URI {
        val ttcdnId = "ttcdn"
        val videoTagName = "video"

        val anySelector = By.cssSelector("#$ttcdnId, $videoTagName")
        val element = wait.until { findElements(anySelector).first() }

        if (element.getAttribute("id") == ttcdnId) {
            switchTo().frame(element)
        }

        val playerSelector = By.tagName("video")
        wait.until(elementToBeClickable(playerSelector))
        return URI.create(findElement(playerSelector).findElement(By.tagName("source")).getAttribute("src"))
    }

    companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendText(ChronoField.MONTH_OF_YEAR)
            .appendLiteral(" ")
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(", ")
            .appendValue(ChronoField.YEAR)
            .toFormatter(Locale.UK)
        private val FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS")
    }
}
