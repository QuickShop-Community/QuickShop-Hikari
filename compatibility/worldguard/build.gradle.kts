plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17") {
        exclude("org.flywaydb", "flyway-core")
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.guava", "guava")
        exclude("org.antlr", "antlr4")
        exclude("org.antlr", "antlr4-runtime")
        exclude("it.unimi.dsi", "fastutil")
        exclude("com.google.code.gson", "gson")
        exclude("com.google.code.findbugs", "jsr305")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("org.mozilla", "rhino-runtime")
    }
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-WorldGuard")
}
