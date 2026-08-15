rootProject.name = "quickshop-hikari"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://api.modrinth.com/maven")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://jitpack.io") {
            metadataSources {
                mavenPom()
                artifact()
            }
        }
        maven("https://www.jitpack.io") {
            metadataSources {
                mavenPom()
                artifact()
            }
        }
        maven("https://m2.dv8tion.net/releases")
        maven("https://maven.devs.beer/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://maven.mohistmc.com/")
        maven("https://nexus.liggesmeyer.net/repository/maven-releases/")
        maven("https://nexus.liggesmeyer.net/repository/maven-snapshots/")
        maven("https://nexus.scarsz.me/content/groups/public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.bg-software.com/repository/api/")
        maven("https://repo.bluecolored.de/releases")
        maven("https://repo.codemc.io/repository/creatorfromhell/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-snapshots")
        maven("https://repo.crazycrew.us/releases")
        maven("https://repo.dustplanet.de/artifactory/libs-release-local/")
        maven("https://repo.essentialsx.net/releases/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.georgev22.com/releases")
        maven("https://repo.glaremasters.me/repository/towny/")
        maven("https://repo.jsinco.dev/releases")
        maven("https://repo.magmaguy.com/releases")
        maven("https://repo.mikeprimm.com/")
        maven("https://repo.minebench.de/")
        maven("https://repo.nexomc.com/releases")
        maven("https://repo.nightexpressdev.com/releases")
        maven("https://repo.opencollab.dev/main")
        maven("https://repo.opencollab.dev/maven-snapshots")
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.songoda.com/repository/minecraft-plugins/")
        maven("https://repo.tcoded.com/releases")
        maven("https://repo.thenextlvl.net/releases")
        maven("https://repo.william278.net/releases")
        maven("https://repo.xenondevs.xyz/releases")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

include(
    "quickshop-common",
    "quickshop-api",
    "quickshop-bukkit",
)

include("platform:quickshop-platform-interface")
project(":platform:quickshop-platform-interface").projectDir = file("platform/quickshop-platform-interface")
include("platform:quickshop-platform-paper")
project(":platform:quickshop-platform-paper").projectDir = file("platform/quickshop-platform-paper")

include("compatibility:common")
project(":compatibility:common").projectDir = file("compatibility/common")

listOf(
    "advancedregionmarket", "bentobox", "bolt", "bungeecord", "bungeecord-geyser",
    "chestprotect", "clearlag", "dominion", "ecoenchants", "elitemobs", "griefprevention",
    "husktowns", "itemsadder", "lands", "matcherplus", "openinv", "plotsquared", "reforges",
    "residence", "simpleclaimsystem", "slimefun", "superiorskyblock", "towny",
    "ultimateclaims", "velocity", "voidchest", "worldguard",
).forEach {
    include("compatibility:$it")
    project(":compatibility:$it").projectDir = file("compatibility/$it")
}

listOf(
    "bluemap", "discordsrv", "discount", "displaycontrol", "dyesigns", "dynmap", "limited",
    "list", "pl3xmap", "plan", "quests", "reremake-migrator", "shopitemonly", "squaremap",
).forEach {
    include("addon:$it")
    project(":addon:$it").projectDir = file("addon/$it")
}
