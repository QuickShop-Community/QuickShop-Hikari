import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("quickshop.java-conventions")
}

apply<ShadowPlugin>()

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")

    manifest {
        attributes["Main-Class"] = "com.ghostchu.quickshop.bootstrap.Bootstrap"
    }

    relocate("net.tnemc", "com.ghostchu.quickshop.shade.tne")
    relocate("cc.carm.lib", "com.ghostchu.quickshop.shade.carm")
    relocate("com.tcoded", "com.ghostchu.quickshop.shade.com.tcoded")
    relocate("io.papermc.lib.", "com.ghostchu.quickshop.shade.io.papermc.lib.")
    relocate("de.tr7zw.changeme.nbtapi", "com.ghostchu.quickshop.shade.de.tr7zw.changeme.nbtapi")
    relocate("org.bstats", "com.ghostchu.quickshop.shade.org.bstats")
    relocate("de.themoep.minedown.", "com.ghostchu.quickshop.shade.de.themoep.minedown.")
    relocate("com.alessiodp.libby.", "com.ghostchu.quickshop.shade.com.alessiodp.libby.")
    relocate(
        "com.ghostchu.lib.unofficial.com.alessiodp.libby.",
        "com.ghostchu.quickshop.shade.com.ghostchu.lib.unofficial.com.alessiodp.libby."
    )
    relocate("com.mohistmc.", "com.ghostchu.quickshop.shade.com.mohistmc.")
    relocate("io.vertx.", "com.ghostchu.quickshop.shade.io.vertx.")
    relocate("com.rollbar.", "com.ghostchu.quickshop.shade.com.rollbar.")
    relocate("dev.faststats.", "com.ghostchu.quickshop.shade.faststats")

    exclude(
        "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.kotlin_module",
        "META-INF/*.txt", "META-INF/proguard/**", "META-INF/versions/9/**",
        "*License*", "*LICENSE*"
    )

    dependencies {
        include(dependency("net.tnemc:.*:.*"))
        include(dependency("com.tcoded:.*:.*"))
        include(dependency("com.ghostchu:quickshop.*:.*"))
        include(dependency("com.ghostchu.quickshop.compatibility:.*:.*"))
        include(dependency("com.ghostchu.quickshop.addon:.*:.*"))
        include(dependency("com.ghostchu:simplereloadlib:.*"))
        include(dependency("de.tr7zw:item-nbt-api:.*"))
        exclude(dependency("io.papermc:paperlib:.*"))
        include(dependency("org.bstats:.*:.*"))
        include(dependency("com.alessiodp.libby:libby-bukkit:.*"))
        include(dependency("com.alessiodp.libby:libby-core:.*"))
        include(dependency("com.ghostchu.lib.unofficial.com.alessiodp.libby:libby-bukkit:.*"))
        include(dependency("com.ghostchu.lib.unofficial.com.alessiodp.libby:libby-core:.*"))
        include(dependency("io.vertx:vertx-core:.*"))
        include(dependency("io.vertx:vertx-web:.*"))
        include(dependency("org.eclipse.aether:.*:.*"))
        include(dependency("dev.dejvokep:boosted-yaml:.*"))
        include(dependency("org.slf4j:slf4j-jdk14:.*"))
        include(dependency("cc.carm.lib:.*:.*"))
        include(dependency("com.rollbar:.*:.*"))
        include(dependency("dev.faststats.metrics:.*:.*"))
        exclude(dependency("net.kyori:adventure-api:.*"))
    }
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}
