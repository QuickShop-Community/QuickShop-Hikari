plugins {
    id("quickshop.compat-conventions")
}

project.extra["quickshopArtifactId"] = "Bolt"

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("org.popcraft:bolt-bukkit:1.1.33")
    compileOnly("org.popcraft:bolt-common:1.1.33")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-Bolt")
}
