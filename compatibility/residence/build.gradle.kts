plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.github.Zrips:Residence:6.0.0.1") { isTransitive = false }
    compileOnly("com.github.Zrips:CMILib:1.5.6.3")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-Residence")
}
