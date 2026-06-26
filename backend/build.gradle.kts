import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    application
    id("com.gradleup.shadow") version "8.3.6"

    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("de.mkammerer:argon2-jvm:2.12")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Needed for the Scryfall and TCGdex API logic from the groupmate project.
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")

    testImplementation(kotlin("test"))
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    ignoreFailures = true
}

ktlint {
    outputToConsole.set(true)
    ignoreFailures.set(true)
}

tasks.register("quality") {
    group = "verification"
    description = "Runs all code quality tools."
    dependsOn("ktlintCheck", "detekt")
}

application {
    mainClass.set("MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}
