plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("net.alex9849.advancedregionmarket:advancedregionmarket:3.5.5") {
        exclude("com.github.alex9849.advanced-region-market", "mc1-14adapter")
        exclude("com.github.alex9849.advanced-region-market", "mc1-20adapter")
        exclude("com.github.alex9849.advanced-region-market", "placeholder-api-addon")
        exclude("com.github.alex9849.advanced-region-market", "we7adapter")
        exclude("com.github.alex9849.advanced-region-market", "wg7adapter")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-AdvancedRegionMarket")
}
