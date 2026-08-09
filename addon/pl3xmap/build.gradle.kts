plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("maven.modrinth:pl3xmap:1.21.5-527")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-Pl3xMap")
}
