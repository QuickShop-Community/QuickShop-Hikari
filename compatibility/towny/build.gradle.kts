plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("com.palmergames.bukkit.towny:towny:0.103.0.2")
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("net.tnemc:EconomyCore:0.1.3.6-Pre-1") { isTransitive = false }
    implementation(project(":compatibility:common"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-Towny")
}
