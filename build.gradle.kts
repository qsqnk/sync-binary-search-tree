repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.5.31"
}

sourceSets.test {
    java.srcDirs("src/test")
}

dependencies {
    implementation(kotlin("reflect"))
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
    implementation("org.jetbrains.kotlinx:lincheck:2.14.1")
}

tasks.test {
    useJUnitPlatform()
}
