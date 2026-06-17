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

    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("de.mkammerer:argon2-jvm:2.12")
    implementation("com.zaxxer:HikariCP:5.1.0")

    testImplementation(kotlin("test"))
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false

    // Voor nu handig: de CI faalt niet direct op alle bestaande code smells.
    // Je krijgt wel een rapport.
    ignoreFailures = true
}

ktlint {
    outputToConsole.set(true)

    // Voor nu handig: de CI blijft groen, maar rapport/check draait wel.
    // Later kun je dit op false zetten als je strenger wilt zijn.
    ignoreFailures.set(true)
}

tasks.register("quality") {
    group = "verification"
    description = "Runs all code quality tools."
    dependsOn("ktlintCheck", "detekt")
}

application {
    mainClass.set("com.example.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}