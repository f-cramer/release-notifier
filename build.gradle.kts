import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("org.springframework.boot") version "4.0.2"
    kotlin("jvm") version "2.3.0"
    kotlin("kapt") version "2.3.0"
    kotlin("plugin.spring") version "2.3.0"
    kotlin("plugin.jpa") version "2.3.0"

    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("dev.detekt") version "2.0.0-alpha.2"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

val isCi = System.getenv("CI") == "true"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:${kotlin.coreLibrariesVersion}"))
    kapt(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:${kotlin.coreLibrariesVersion}"))
    developmentOnly(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:${kotlin.coreLibrariesVersion}"))
    implementation(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    kapt(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))
    developmentOnly(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.seleniumhq.selenium:selenium-firefox-driver")
    implementation("org.seleniumhq.selenium:selenium-support")
    runtimeOnly("org.postgresql:postgresql")
    kapt("org.hibernate.orm:hibernate-processor")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testImplementation("net.datafaker:datafaker:2.5.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property"))
        allWarningsAsErrors = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

ktlint {
    version.set("1.8.0")
    additionalEditorconfig.set(
        mapOf(
            "ktlint_code_style" to "intellij_idea",
            "ktlint_standard_function-expression-body" to "disabled",
            "ktlint_standard_no-line-break-before-assignment" to "disabled",
        ),
    )
    reporters {
        reporter(if (isCi) ReporterType.CHECKSTYLE else ReporterType.HTML)
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files(".config/detekt.yml"))
}
