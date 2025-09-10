import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.20"
    kotlin("kapt") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.2.20"

    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

val isCi = System.getenv("CI") == "true"

ext["kotlin.version"] = kotlin.coreLibrariesVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jsoup:jsoup:1.21.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("org.seleniumhq.selenium:selenium-firefox-driver")
    implementation("org.seleniumhq.selenium:selenium-support")
    runtimeOnly("org.postgresql:postgresql")
    kapt("org.hibernate.orm:hibernate-jpamodelgen")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testImplementation("net.datafaker:datafaker:2.4.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(listOf("-Xjsr305=strict", "-Xsuppress-version-warnings"))
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
    version.set("1.2.1")
    additionalEditorconfig.set(
        mapOf(
            "ktlint_code_style" to "intellij_idea",
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

dependencyManagement {
    configurations.getByName("detekt") {
        dependencies {
            dependencySet("org.jetbrains.kotlin:2.0.21") {
                entry("kotlin-compiler-embeddable")
            }
        }
    }
}
