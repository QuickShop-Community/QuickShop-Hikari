plugins {
    id("buildlogic.java-conventions")
    id("com.gradleup.shadow") version "9.0.0-beta16" apply true
}

dependencies {
    api(libs.org.apache.commons.commons.lang3)
    api(libs.org.slf4j.slf4j.jdk14)
    api(libs.com.google.code.gson.gson)
    api(libs.com.ghostchu.simplereloadlib)
    api(libs.cc.carm.lib.easysql.api)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    api(libs.com.vdurmont.semver4j)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.adventure.api)
    compileOnly(libs.net.kyori.adventure.key)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.adventure.text.logger.slf4j)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.adventure.text.minimessage)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.adventure.text.serializer.json)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.adventure.text.serializer.legacy)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.adventure.text.serializer.plain)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.examination.api)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.net.kyori.examination.string)
    api(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
}

tasks {
    compileJava {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    jar {
        dependsOn(shadowJar)
        archiveFileName = "original-QuickShop-Common-${project.version}.jar"
    }

    shadowJar {
        archiveFileName = "QuickShop-Common-${project.version}.jar"
        archiveClassifier = ""

        configurations = listOf(project.configurations.shadow.get())
    }
}

description = "quickshop-common"
