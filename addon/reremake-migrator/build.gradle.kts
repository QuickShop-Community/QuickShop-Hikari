import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("quickshop.java-conventions")
    id("quickshop.resource-filtering-conventions")
}

apply<ShadowPlugin>()

group = "com.ghostchu.quickshop.addon"

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("io.vavr:vavr:0.10.7")
    compileOnly("org.maxgamer:QuickShop:5.1.2.5-SNAPSHOT") { isTransitive = false }
    implementation("de.themoep:minedown-adventure:1.7.4-SNAPSHOT")
    implementation("com.ghostchu.thirdparty:JsonConfiguration:1.2-20230922.165143-1")
    implementation("net.minidev:json-smart:1.1.1")
}

tasks.withType<ShadowJar>().configureEach {
    archiveBaseName.set("Addon-Reremake-Migrator")
    archiveClassifier.set("")

    relocate("de.themoep.minedown.", "com.ghostchu.quickshop.addon.reremakemigrator.shade.de.themoep.minedown.")
    relocate("com.dumptruckman.", "com.ghostchu.quickshop.addon.reremakemigrator.shade.com.dumptruckman.")
    relocate("net.minidev.", "com.ghostchu.quickshop.addon.reremakemigrator.shade.net.minidev.")

    exclude(
        "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.kotlin_module",
        "META-INF/*.txt", "META-INF/proguard/**", "META-INF/services/**", "META-INF/versions/9/**",
        "*License*", "*LICENSE*"
    )

    dependencies {
        include(dependency("de.themoep:minedown-adventure:.*"))
        include(dependency("com.ghostchu.thirdparty:JsonConfiguration:.*"))
        include(dependency("net.minidev:json-smart:.*"))
    }
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<Jar>("sourcesJar") {
    enabled = false
}
