package de.cramer.releasenotifier.providers.tabletoptactics

import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsConfiguration
import de.cramer.releasenotifier.providers.tabletoptactics.entities.TabletopTacticsVideo
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Service
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

@Service
class TabletopTacticsConfigurationService {
    fun update(configuration: TabletopTacticsConfiguration) {
        val options = FirefoxOptions().apply {
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

            val videosSelector = By.cssSelector(".type-post.status-publish.format-video")
            wait.until(elementToBeClickable(videosSelector))

            val videoElements = driver.findElements(videosSelector).filterNotNull()
            videoElements.forEach {
                driver.updateVideo(it, wait, configuration)
            }
        } finally {
            driver.quit()
        }
    }

    private fun FirefoxDriver.updateVideo(element: WebElement, wait: WebDriverWait, configuration: TabletopTacticsConfiguration) {
        val details = element.findElement(By.className("qt-details"))
        var dateText = Jsoup.parse(details.getAttribute("outerHTML").trim())
            .select("body > ${details.tagName}").textNodes()
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }
            .joinToString(separator = "")
        val date = LocalDate.parse(dateText.trim(), DATE_FORMATTER)
        val name = element.findElement(By.cssSelector(".qt-title a")).text.trim()
        val background = element.findElement(By.className("qt-header-bg"))
        val imageUrl = URI.create(background.getAttribute("data-bgimage"))
        executeScript("""arguments[0].scrollIntoView({ block: "center", inline: "center" })""", background)
        Actions(this)
            .keyDown(Keys.CONTROL)
            .moveToElement(background)
            .click()
            .perform()

        val originalTab = windowHandle
        val lastTab = windowHandles.last()

        switchTo().window(lastTab)
        wait.until(elementToBeClickable(By.id("qtcontents")))

        val videoUrl = getYoutubeUrl()
            ?: getTTUrl(wait)

        val existingVideo = configuration.videos.find {
            it.name == name && it.date == date
        }
        if (existingVideo == null) {
            val video = TabletopTacticsVideo(0, name, date, videoUrl, imageUrl, configuration)
            configuration.videos += video
        } else {
            existingVideo.url = videoUrl
            existingVideo.thumbnail = imageUrl
        }

        close()

        switchTo().window(originalTab)
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

    private fun WebDriver.getTTUrl(wait: Wait<WebDriver>): URI {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ttcdn")))
        val playerSelector = By.tagName("video")
        wait.until(elementToBeClickable(playerSelector))
        return URI.create(findElement(playerSelector).findElement(By.tagName("source")).getAttribute("src"))
    }

    companion object {
        val DATE_FORMATTER = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendText(ChronoField.MONTH_OF_YEAR)
            .appendLiteral(" ")
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(", ")
            .appendValue(ChronoField.YEAR)
            .toFormatter(Locale.UK)
    }
}
