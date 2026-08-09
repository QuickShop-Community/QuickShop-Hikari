plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.willfp:EcoEnchants:13.9.0")
    compileOnly("com.willfp:libreforge-loader:5.7.0")
    compileOnly("com.willfp:eco:6.75.2") {
        exclude("org.apache.maven", "maven-artifact")
        exclude("com.github.ben-manes.caffeine", "caffeine")
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-text-serializer-gson")
        exclude("net.kyori", "adventure-text-serializer-legacy")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-EcoEnchants")
}
