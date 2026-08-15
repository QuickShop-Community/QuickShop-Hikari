plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
    compileOnly("org.geysermc.geyser:api:2.1.0-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-BungeeCord-Geyser")
}
