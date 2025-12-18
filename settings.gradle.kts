pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
    }
    plugins {
        id("fabric-loom") version "1.14-SNAPSHOT"
        id("dev.kikugie.stonecutter") version "0.5.1"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5.1"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions("1.21.9", "1.21.10", "1.21.11")
        vcsVersion = "1.21.11"  // PRIMARY VERSION - always latest
    }
}

rootProject.name = "simplefps"
