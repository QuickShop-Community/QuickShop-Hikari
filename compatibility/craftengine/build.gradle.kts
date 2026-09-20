plugins {
    id("quickshop.compat-conventions")
}

//CraftEngine API version this compatibility module is built against
val craftEngineVersion = "26.9.1"

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("net.momirealms:craft-engine-bukkit:$craftEngineVersion")
    compileOnly("net.momirealms:craft-engine-core:$craftEngineVersion")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-CraftEngine")
}
