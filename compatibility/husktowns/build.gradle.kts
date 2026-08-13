plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("net.william278.husktowns:husktowns-bukkit:3.1.4")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-HuskTowns")
}
