plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.songoda:UltimateClaims:3.0.2-SNAPSHOT")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-UltimateClaims")
}
