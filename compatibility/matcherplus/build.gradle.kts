plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.nexomc:nexo:1.1.0")
    compileOnly("com.dre.brewery:BreweryX:3.4.3")
    compileOnly("com.badbones69.crazycrates:crazycrates-paper-api:1.21.7-1e861ff") {
        exclude("dev.triumphteam", "triumph-cmd-bukkit")
        exclude("CrazyCrates", "crazycrates-core")
    }
    implementation("de.dustplanet:silkspawners:8.3.0") { isTransitive = false }
    compileOnly("io.th0rgal:oraxen:1.189.0") {
        exclude("me.gabytm.util", "actions-spigot")
        exclude("org.jetbrains", "annotations")
        exclude("com.ticxo", "PlayerAnimator")
        exclude("com.github.stefvanschie.inventoryframework", "IF")
        exclude("io.th0rgal", "protectionlib")
        exclude("dev.triumphteam", "triumph-gui")
        exclude("org.bstats", "bstats-bukkit")
        exclude("com.jeff-media", "custom-block-data")
        exclude("com.jeff-media", "persistent-data-serializer")
        exclude("com.jeff_media", "MorePersistentDataTypes")
        exclude("gs.mclo", "java")
    }
    implementation("com.github.Slimefun:Slimefun4:RC-37")
    compileOnly("xyz.xenondevs.nova:nova-api:0.18")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-MatcherPlus")
}
