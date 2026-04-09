plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("app.MainKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.4.2"

dependencies {
    implementation(kotlin("stdlib"))

    implementation(platform("io.kriptal.ethers:ethers-bom:1.6.0"))
    implementation("io.kriptal.ethers:ethers-core")
    implementation("io.kriptal.ethers:ethers-crypto")
    implementation("io.kriptal.ethers:ethers-signers")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    implementation("ch.qos.logback:logback-classic:1.5.16")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

tasks.test {
    useJUnitPlatform()
}