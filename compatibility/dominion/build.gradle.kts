plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("cn.lunadeer:DominionAPI:4.5.0")
    compileOnly(project(":quickshop-bukkit"))
    implementation(project(":compatibility:common"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-Dominion")
}
