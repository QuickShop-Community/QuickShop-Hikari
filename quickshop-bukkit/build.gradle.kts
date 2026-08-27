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

    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("com.github.retrooper:packetevents-spigot:2.12.2")
    compileOnly("net.luckperms:api:5.5")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.20") { isTransitive = false }
    compileOnly("me.clip:placeholderapi:2.11.6") { isTransitive = false }

    api("net.tnemc:TNML-Folia:1.7.0.1-SNAPSHOT-5") {
        exclude("net.tnemc", "TNIL-Bukkit")
        exclude("net.tnemc", "TNIL-CORE")
    }
    api("net.tnemc:TNML-Bukkit:1.7.0.1-SNAPSHOT-5") {
        exclude("net.tnemc", "TNIL-Bukkit")
        exclude("net.tnemc", "TNIL-CORE")
        exclude("net.kyori")
    }
    api("net.tnemc:TNIL-Bukkit:0.2.0.3-SNAPSHOT-1") {
        exclude("net.kyori")
    }
    api("net.tnemc:TNIL-Paper:0.2.0.3-SNAPSHOT-1") {
        exclude("net.kyori")
    }
    api("net.tnemc:TNML-CORE:1.7.0.1-SNAPSHOT-5") {
        exclude("net.tnemc", "TNIL-Core")
        exclude("net.kyori")
    }
    api("net.tnemc:TNIL-Core:0.2.0.3-SNAPSHOT-1") {
        exclude("net.kyori")
    }

    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("com.konghq:unirest-java:3.14.5")
    compileOnly("net.sourceforge.csvjdbc:csvjdbc:1.0.42")
    compileOnly("org.dom4j:dom4j:2.1.4")
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        exclude("org.yaml", "snakeyaml")
        exclude("com.google.errorprone", "error_prone_annotations")
        exclude("org.checkerframework", "checker-qual")
    }
    compileOnly("com.ghostchu.crowdin:crowdinota:1.0.3")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.rollbar:rollbar-java:2.0.0") {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation("com.rollbar:rollbar-api:2.0.0")
    api("cc.carm.lib:easysql-hikaricp:0.4.7")
    api("dev.faststats.metrics:bukkit:0.17.2")
    api("org.apache.commons:commons-compress:1.26.2")
    api("com.tcoded:FoliaLib:0.5.1")
    api("com.ghostchu.lib.unofficial.com.alessiodp.libby:libby-core:2.0.2-SNAPSHOT")
    api("com.ghostchu.lib.unofficial.com.alessiodp.libby:libby-bukkit:2.0.2-SNAPSHOT")

    val worldeditExcludes: ExternalModuleDependency.() -> Unit = {
        exclude("org.bstats")
        exclude("it.unimi.dsi", "fastutil")
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
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.12.0", worldeditExcludes)
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.17", worldeditExcludes)
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
