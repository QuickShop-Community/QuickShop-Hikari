plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("com.github.jikoo.OpenInv:openinvapi:5.1.6")
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-OpenInv")
}
