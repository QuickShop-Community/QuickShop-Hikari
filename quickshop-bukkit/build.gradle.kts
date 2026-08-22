import com.ghostchu.quickshop.buildlogic.GitInfoValueSource
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("quickshop.core-conventions")
    id("quickshop.shadow-conventions")
}

dependencies {
    compileOnly(libs.paper.api.pinned)
    api(project(":quickshop-api"))
    implementation(project(":platform:quickshop-platform-paper"))

    compileOnly(libs.protocol.lib)
    compileOnly(libs.packet.events.spigot)
    compileOnly(libs.luckperms.api)

    compileOnly(libs.vault.unlocked.api) {
        isTransitive = false
    }

    compileOnly(libs.placeholder.api) {
        isTransitive = false
    }

    api(libs.tnml.folia) {
        exclude("net.tnemc", "TNIL-Bukkit")
        exclude("net.tnemc", "TNIL-CORE")
    }

    api(libs.tnml.bukkit) {
        exclude("net.tnemc", "TNIL-Bukkit")
        exclude("net.tnemc", "TNIL-CORE")
        exclude("net.kyori")
    }

    api(libs.tnil.bukkit) {
        exclude("net.kyori")
    }

    api(libs.tnil.paper) {
        exclude("net.kyori")
    }

    api(libs.tnml.core) {
        exclude("net.tnemc", "TNIL-Core")
        exclude("net.kyori")
    }

    api(libs.tnil.core) {
        exclude("net.kyori")
    }

    api(libs.paperlib)

    compileOnly(libs.h2)
    compileOnly(libs.unirest.java)
    compileOnly(libs.csvjdbc)
    compileOnly(libs.dom4j)

    compileOnly(libs.essentials.x) {
        exclude("io.papermc", "paperlib")
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.errorprone", "error_prone_annotations")
        exclude("org.checkerframework", "checker-qual")
    }

    compileOnly(libs.crowdinota)

    implementation(libs.bstats.bukkit)

    implementation(libs.rollbar.java) {
        exclude("org.slf4j", "slf4j-api")
    }

    implementation(libs.rollbar.api)

    api(libs.easysql.hikaricp)
    api(libs.faststats.bukkit)
    api(libs.commons.compress)
    api(libs.folia.lib)
    api(libs.libby.core)
    api(libs.libby.bukkit)

    val worldeditExcludes: ExternalModuleDependency.() -> Unit = {
        exclude("org.bstats")
        exclude("it.unimi.dsi", "fastutil")
        exclude("io.papermc", "paperlib")
        exclude("org.antlr", "antlr4")
        exclude("org.antlr", "antlr4-runtime")
        exclude("com.google.code.gson", "gson")
        exclude("com.google.guava", "guava")
        exclude("com.sk89q", "jchronic")
        exclude("com.sk89q.lib", "jlibnoise")
        exclude("org.enginehub.lin-bus.format", "lin-bus-format-snbt")
        exclude("org.enginehub.lin-bus", "lin-bus-tree")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("com.thoughtworks.paranamer", "paranamer")
        exclude("org.mozilla", "rhino-runtime")
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.code.findbugs", "jsr305")
    }

    compileOnly(libs.fawe.core, worldeditExcludes)
    compileOnly(libs.worldedit.bukkit, worldeditExcludes)
}

sourceSets {
    main {
        resources {
            srcDir("../crowdin")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    val pluginVersion = project.version.toString()
    val pluginArtifactId = project.name
    filesMatching("plugin.yml") {
        expand(mapOf("project" to mapOf("version" to pluginVersion, "artifactId" to pluginArtifactId)))
    }

    val gitInfo = providers.of(GitInfoValueSource::class) {}
    filesMatching("BUILDINFO") {
        filter { line ->
            val tokens = gitInfo.get() + mapOf("git.build.version" to pluginVersion)
            tokens.entries.fold(line) { acc, (key, value) -> acc.replace("\${$key}", value) }
        }
    }
}

tasks.withType<ShadowJar>().configureEach {
    archiveBaseName.set("QuickShop-Hikari")
}