plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":quickshop-api"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("xyz.jpenilla:squaremap-api:1.3.9")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-Squaremap")
}
