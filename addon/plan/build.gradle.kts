plugins {
    id("quickshop.addon-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.github.plan-player-analytics:Plan:5.6.2965") {
        exclude("com.google.dagger", "dagger")
        exclude("javax.inject", "javax.inject")
    }
    implementation("com.github.juliomarcopineda:jdbc-stream:0.1.1")
    compileOnly("cc.carm.lib:easysql-api:0.4.7")
    compileOnly(project(":platform:quickshop-platform-interface"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Addon-Plan")
}
