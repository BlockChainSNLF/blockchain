plugins {
    kotlin("jvm") version "2.0.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    implementation(platform("io.kriptal.ethers:ethers-bom:1.6.0"))
    implementation("io.kriptal.ethers:ethers-core")
    implementation("io.kriptal.ethers:ethers-crypto")
    implementation("io.kriptal.ethers:ethers-signers")
}

tasks.test {
    useJUnitPlatform()
}