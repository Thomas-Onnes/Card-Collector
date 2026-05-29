plugins {
    kotlin("jvm") version "2.2.0"
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
}

tasks.test {
    useJUnitPlatform()
}