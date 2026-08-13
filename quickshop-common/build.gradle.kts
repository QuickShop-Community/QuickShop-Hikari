plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api(libs.commons.lang3)
    api(libs.slf4j.jdk14)
    api(libs.gson)
    api(libs.simple.reload.lib)
    api(libs.easysql.api)
    api(libs.semver4j)

    compileOnly(libs.adventure.api) {
        exclude("net.kyori", "adventure-key")
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "examination-string")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.adventure.key) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "examination-string")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.adventure.text.logger.slf4j) {
        exclude("net.kyori", "adventure-api")
        exclude("org.slf4j", "slf4j-api")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.adventure.text.minimessage) {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }

    api(libs.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.adventure.text.serializer.json) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "option")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.adventure.text.serializer.legacy) {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.adventure.text.serializer.plain) {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.examination.api) {
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }

    compileOnly(libs.examination.string) {
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
}
