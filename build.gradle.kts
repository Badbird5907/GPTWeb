plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
}

group = "dev.badbird.gpt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.theokanning.openai-gpt3-java:service:0.18.2")
    implementation("org.jsoup:jsoup:1.17.2")
}

tasks.test {
    useJUnitPlatform()
}