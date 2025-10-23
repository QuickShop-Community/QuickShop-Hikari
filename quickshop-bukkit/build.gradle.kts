plugins {
    id("buildlogic.java-conventions")
    id("com.gradleup.shadow") version "9.0.0-beta16" apply true
}

dependencies {
    shadow(project(":quickshop-common"))
    shadow(project(":quickshop-api"))
    shadow(project(":quickshop-platform-interface"))
    shadow(project(":quickshop-platform-paper"))
    shadow(libs.net.tnemc.tnml.folia)
    shadow(libs.net.tnemc.tnml.bukkit)
    shadow(libs.net.tnemc.tnil.bukkit)
    shadow(libs.net.tnemc.tnml.core)
    shadow(libs.net.tnemc.tnil.core)
    shadow(libs.org.bstats.bstats.bukkit)
    shadow(libs.cc.carm.lib.easysql.hikaricp)
    shadow(libs.org.apache.commons.commons.compress)
    shadow(libs.com.tcoded.folialib)
    shadow(libs.com.ghostchu.lib.unofficial.com.alessiodp.libby.libby.core)
    shadow(libs.com.ghostchu.lib.unofficial.com.alessiodp.libby.libby.bukkit)
    shadow(libs.org.apache.commons.commons.lang3)
    shadow(libs.org.slf4j.slf4j.jdk14)
    shadow(libs.com.google.code.gson.gson)
    shadow(libs.com.ghostchu.simplereloadlib)
    shadow(libs.cc.carm.lib.easysql.api)
    shadow(libs.com.vdurmont.semver4j)
    shadow(libs.net.kyori.adventure.text.serializer.ansi) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly(libs.com.comphenix.protocol.protocollib)
    compileOnly(libs.com.github.retrooper.packetevents.spigot)
    compileOnly(libs.net.milkbowl.vault.vaultunlockedapi)
    compileOnly(libs.com.github.milkbowl.vaultapi)
    compileOnly(libs.me.clip.placeholderapi)
    compileOnly(libs.net.tnemc.economycore)
    compileOnly(libs.com.h2database.h2)
    compileOnly(libs.com.konghq.unirest.java)
    compileOnly(libs.net.sourceforge.csvjdbc.csvjdbc)
    compileOnly(libs.org.dom4j.dom4j)
    compileOnly(libs.net.essentialsx.essentialsx) {
        exclude("org.spigotmc", "spigot-api")
    }
    compileOnly(libs.com.ghostchu.crowdin.crowdinota)
    compileOnly(libs.com.rollbar.rollbar.java) {
        exclude("org.slf4j", "slf4j-api")
    }
}

tasks {
    compileJava {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    jar {
        dependsOn(shadowJar)
        archiveFileName = "original-QuickShop-Bukkit-${project.version}.jar"
    }

    shadowJar {
        archiveFileName = "QuickShop-Bukkit-${project.version}.jar"
        archiveClassifier = ""

        configurations = listOf(project.configurations.shadow.get())
    }
}

description = "QuickShop-Hikari"