import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    `java-library`
    `maven-publish`
    jacoco
}

val appGroupId = "com.github.aivanovski"
val appArtifactId = "keepass-tree-diff"
val appVersion = "0.6.0"

group = appGroupId
version = appVersion

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    withSourcesJar()
    withJavadocJar()
}

tasks.jacocoTestReport {
    reports {
        val coverageDir = layout.buildDirectory.dir("reports/coverage")
        csv.required.set(true)
        csv.outputLocation.set(coverageDir.map { it.file("coverage.csv") })
        html.required.set(true)
        html.outputLocation.set(coverageDir)
    }

    dependsOn(allprojects.map { it.tasks.named<Test>("test") })
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

dependencies {
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.mockk)

    implementation(libs.kotlin.stdlib.jdk8)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = appGroupId
            artifactId = appArtifactId
            version = appVersion

            from(components["java"])
        }
    }
}
