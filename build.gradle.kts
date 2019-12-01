import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.60"
    id("org.jmailen.kotlinter") version "2.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.60"
    jacoco
}

version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")

    // For reflection in tests
    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = false
    }
}
