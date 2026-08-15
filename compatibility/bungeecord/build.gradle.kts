plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-BungeeCord")
}
