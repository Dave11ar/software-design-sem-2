import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.21"
}

group = "org.example"
version = "1.0-M1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:1.0-M1-1.4.0-rc")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.typesafe:config:1.4.2")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    implementation("io.reactivex:rxjava:1.3.8")
    implementation("org.mongodb:mongodb-driver-rx:1.5.0")
    implementation("io.reactivex:rxnetty-common:0.5.3")
    implementation("io.reactivex:rxnetty-http:0.5.3")
    implementation("org.slf4j:slf4j-log4j12:1.7.25")
    implementation("org.slf4j:slf4j-api:2.0.5")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}