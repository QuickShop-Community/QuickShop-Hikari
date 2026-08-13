plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit:7.5.13") {
        exclude("com.github.EngineHub", "SquirrelID")
        exclude("com.intellectualsites.arkitektonika", "Arkitektonika-Client")
        exclude("com.intellectualsites.paster", "Paster")
        exclude("dev.notmyfault.serverlib", "ServerLib")
        exclude("org.enginehub", "squirrelid")
        exclude("net.kyori", "adventure-platform-bukkit")
        exclude("io.papermc", "paperlib")
        exclude("org.bstats", "bstats-bukkit")
        exclude("com.intellectualsites.prtree", "PRTree")
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("aopalliance", "aopalliance")
        exclude("org.checkerframework", "checker-qual")
        exclude("cloud.commandframework", "cloud-services")
        exclude("com.google.inject.extensions", "guice-assistedinject")
        exclude("com.github.spotbugs", "spotbugs-annotations")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("com.google.code.gson", "gson")
        exclude("org.yaml", "snakeyaml")
    }
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.17") {
        exclude("org.bstats")
        exclude("it.unimi.dsi", "fastutil")
        exclude("io.papermc", "paperlib")
        exclude("org.antlr", "antlr4")
        exclude("org.antlr", "antlr4-runtime")
        exclude("com.google.code.gson", "gson")
        exclude("com.google.guava", "guava")
        exclude("com.sk89q", "jchronic")
        exclude("com.sk89q.lib", "jlibnoise")
        exclude("org.enginehub.lin-bus.format", "lin-bus-format-snbt")
        exclude("org.enginehub.lin-bus", "lin-bus-tree")
        exclude("org.mozilla", "rhino-runtime")
        exclude("com.thoughtworks.paranamer", "paranamer")
        exclude("org.yaml", "snakeyaml")
        exclude("org.apache.logging.log4j", "log4j-api")
    }
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-PlotSquared")
}
