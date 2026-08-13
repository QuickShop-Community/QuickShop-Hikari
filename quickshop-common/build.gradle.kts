plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api("org.apache.commons:commons-lang3:3.18.0")
    api("org.slf4j:slf4j-jdk14:2.0.17")
    api("com.google.code.gson:gson:2.13.1")
    api("com.ghostchu:simplereloadlib:1.1.2")
    api("cc.carm.lib:easysql-api:0.4.7")
    api("com.vdurmont:semver4j:3.1.0")

    compileOnly("net.kyori:adventure-api:5.1.1") {
        exclude("net.kyori", "adventure-key")
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "examination-string")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:adventure-key:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "examination-string")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:adventure-text-logger-slf4j:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("org.slf4j", "slf4j-api")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:adventure-text-minimessage:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
    api("net.kyori:adventure-text-serializer-ansi:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:adventure-text-serializer-json:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "option")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:adventure-text-serializer-legacy:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:adventure-text-serializer-plain:5.1.1") {
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:examination-api:1.3.0") {
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
    compileOnly("net.kyori:examination-string:1.3.0") {
        exclude("net.kyori", "examination-api")
        exclude("net.kyori", "adventure-api")
        exclude("org.jetbrains", "annotations")
    }
}
