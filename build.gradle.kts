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
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
    implementation("org.jetbrains.kotlinx:lincheck:2.14.1")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    withType<Test> {
        maxParallelForks = 1
        jvmArgs("--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
            "--add-exports", "java.base/jdk.internal.util=ALL-UNNAMED")
    }
    test {
        useJUnitPlatform()
    }
}
