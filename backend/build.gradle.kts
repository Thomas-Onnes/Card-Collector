plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.6"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    application
}

application {
    mainClass.set("MainKt")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
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
kotlin {
    jvmToolchain(21)
}