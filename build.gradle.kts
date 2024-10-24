/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    application
    java
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://git.informatik.uni-hamburg.de/api/v4/groups/sane-public/-/packages/maven")
    }
}

dependencies {
    implementation(libs.ditto.client)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.javalin)
    implementation(libs.jena)
    implementation(libs.wot.servient)
    testImplementation(libs.bundles.java.testing)
    testRuntimeOnly(libs.junit.engine)
}

group = "io.github.webbasedwodt"

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}