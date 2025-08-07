rootProject.name = "quickshop-hikari"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

//Core Modules
include(":quickshop-common")
include(":quickshop-api")
include(":quickshop-platform-interface")
include(":quickshop-platform-paper")
include(":quickshop-bukkit")


//Addons
include(":bluemap")
include(":discordsrv")
include(":discount")
include(":displaycontrol")
include(":dynmap")
include(":limited")
include(":list")
include(":plan")
include(":reremake-migrator")
include(":shopitemonly")

//Compatibility Modules
include(":common")

include(":advancedregionmarket")
include(":angelchest")
include(":bentobox")
include(":bungeecord")
include(":bungeecord-geyser")
include(":chestprotect")
include(":clearlag")
include(":dominion")
include(":ecoenchants")
include(":elitemobs")
include(":fabledskyblock")
include(":griefprevention")
include(":husktowns")
include(":itemsadder")
include(":iridiumskyblock")
include(":lands")
include(":matcherplus")
include(":openinv")
include(":plotsquared")
include(":reforges")
include(":residence")
include(":slimefun")
include(":superiorskyblock")
include(":towny")
include(":velocity")
include(":voidchest")
include(":worldedit")
include(":worldguard")

//Core Modules
project(":quickshop-platform-interface").projectDir = file("platform/quickshop-platform-interface")
project(":quickshop-platform-paper").projectDir = file("platform/quickshop-platform-paper")

//Addons
project(":bluemap").projectDir = file("addon/bluemap")
project(":discordsrv").projectDir = file("addon/discordsrv")
project(":discount").projectDir = file("addon/discount")
project(":displaycontrol").projectDir = file("addon/displaycontrol")
project(":dynmap").projectDir = file("addon/dynmap")
project(":limited").projectDir = file("addon/limited")
project(":list").projectDir = file("addon/list")
project(":plan").projectDir = file("addon/plan")
project(":reremake-migrator").projectDir = file("addon/reremake-migrator")
project(":shopitemonly").projectDir = file("addon/shopitemonly")

//Compatibility Modules

project(":common").projectDir = file("compatibility/common")

project(":advancedregionmarket").projectDir = file("compatibility/advancedregionmarket")
project(":angelchest").projectDir = file("compatibility/angelchest")
project(":bentobox").projectDir = file("compatibility/bentobox")
project(":bungeecord").projectDir = file("compatibility/bungeecord")
project(":bungeecord-geyser").projectDir = file("compatibility/bungeecord-geyser")
project(":chestprotect").projectDir = file("compatibility/chestprotect")
project(":clearlag").projectDir = file("compatibility/clearlag")
project(":dominion").projectDir = file("compatibility/dominion")
project(":ecoenchants").projectDir = file("compatibility/ecoenchants")
project(":elitemobs").projectDir = file("compatibility/elitemobs")
project(":fabledskyblock").projectDir = file("compatibility/fabledskyblock")
project(":griefprevention").projectDir = file("compatibility/griefprevention")
project(":husktowns").projectDir = file("compatibility/husktowns")
project(":iridiumskyblock").projectDir = file("compatibility/iridiumskyblock")
project(":itemsadder").projectDir = file("compatibility/itemsadder")
project(":lands").projectDir = file("compatibility/lands")
project(":matcherplus").projectDir = file("compatibility/matcherplus")
project(":openinv").projectDir = file("compatibility/openinv")
project(":plotsquared").projectDir = file("compatibility/plotsquared")
project(":reforges").projectDir = file("compatibility/reforges")
project(":residence").projectDir = file("compatibility/residence")
project(":slimefun").projectDir = file("compatibility/slimefun")
project(":superiorskyblock").projectDir = file("compatibility/superiorskyblock")
project(":towny").projectDir = file("compatibility/towny")
project(":voidchest").projectDir = file("compatibility/voidchest")
project(":worldedit").projectDir = file("compatibility/worldedit")
project(":velocity").projectDir = file("compatibility/velocity")
project(":worldguard").projectDir = file("compatibility/worldguard")