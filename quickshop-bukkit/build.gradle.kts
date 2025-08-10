plugins {
    id("buildlogic.java-conventions")
    id("com.gradleup.shadow") version "9.0.0-beta16" apply true
}

dependencies {
    api(project(":quickshop-api"))
    api(project(":quickshop-platform-paper"))
    api(libs.net.tnemc.tnml.folia)
    api(libs.net.tnemc.tnml.bukkit)
    api(libs.net.tnemc.tnil.bukkit)
    api(libs.net.tnemc.tnml.core)
    api(libs.net.tnemc.tnil.core)
    api(libs.org.bstats.bstats.bukkit)
    api(libs.cc.carm.lib.easysql.hikaricp)
    api(libs.org.apache.commons.commons.compress)
    api(libs.com.tcoded.folialib)
    api(libs.com.ghostchu.lib.unofficial.com.alessiodp.libby.libby.core)
    api(libs.com.ghostchu.lib.unofficial.com.alessiodp.libby.libby.bukkit)
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