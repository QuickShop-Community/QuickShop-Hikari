plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.magmaguy:EliteMobs:9.2.3") {
        exclude("cloud.commandframework", "cloud-paper")
        exclude("cloud.commandframework", "cloud-minecraft-extras")
        exclude("io.leangen.geantyref", "geantyref")
        exclude("org.reflections", "reflections")
        exclude("commons-io", "commons-io")
        exclude("net.kyori")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-EliteMobs")
}
