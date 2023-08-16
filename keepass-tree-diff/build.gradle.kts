import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.9.0")
    id("java-library")
    id("maven-publish")
    jacoco
}

val appVersion = "0.1.0"

group = "com.github.ai.keepasstreediff"
version = appVersion

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.jacocoTestReport {
    reports {
        val coverageDir = File("$buildDir/reports/coverage")
        csv.required.set(true)
        csv.outputLocation.set(File(coverageDir, "coverage.csv"))
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
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.2")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.2")
    testImplementation("io.mockk:mockk:1.12.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
}