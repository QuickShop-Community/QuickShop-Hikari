plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(project(":quickshop-api"))
    api(project(":quickshop-platform-paper"))
    api(libs.net.tnemc.tnml.folia)
    api(libs.net.tnemc.tnml.bukkit)
    api(libs.net.tnemc.tnil.bukkit)
    api(libs.net.tnemc.tnml.core)
    api(libs.net.tnemc.tnil.core)
    api(libs.io.papermc.paperlib)
    api(libs.org.bstats.bstats.bukkit)
    api(libs.cc.carm.lib.easysql.hikaricp)
    api(libs.org.apache.commons.commons.compress)
    api(libs.com.tcoded.folialib)
    api(libs.com.ghostchu.lib.unofficial.com.alessiodp.libby.libby.core)
    api(libs.com.ghostchu.lib.unofficial.com.alessiodp.libby.libby.bukkit)
    compileOnly(libs.io.papermc.paper.paper.api.x1)
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
    compileOnly(libs.net.essentialsx.essentialsx)
    compileOnly(libs.com.ghostchu.crowdin.crowdinota)
    compileOnly(libs.com.rollbar.rollbar.java)
}

description = "QuickShop-Hikari"