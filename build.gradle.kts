import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

val isCi = System.getenv("CI") == "true"

ext["kotlin.version"] = kotlin.coreLibrariesVersion

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("org.seleniumhq.selenium:selenium-firefox-driver")
    implementation("org.seleniumhq.selenium:selenium-support")
    runtimeOnly("org.postgresql:postgresql")
    kapt("org.hibernate.orm:hibernate-jpamodelgen")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.0")
    testImplementation("net.datafaker:datafaker:2.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

ktlint {
    version.set("1.0.0")
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
            dependencySet("org.jetbrains.kotlin:1.9.21") {
                entry("kotlin-compiler-embeddable")
            }
        }
    }
}
