plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("world.bentobox:bentobox:2.7.0-SNAPSHOT") {
        exclude("org.eclipse.jdt", "org.eclipse.jdt.annotation")
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-BentoBox")
}
