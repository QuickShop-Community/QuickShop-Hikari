plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("de.bluecolored.bluemap:BlueMapAPI:2.7.2") {
        exclude("com.google.code.gson", "gson")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-BlueMap")
}
