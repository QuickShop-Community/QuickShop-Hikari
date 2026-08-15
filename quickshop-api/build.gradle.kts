plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api(project(":quickshop-common"))
    compileOnly(libs.paper.api)
    api("dev.dejvokep:boosted-yaml:1.3.7") {
        exclude("org.jetbrains", "annotations-java5")
    }
    api("net.tnemc:TNIL-Bukkit:0.2.0.3-SNAPSHOT-1") {
        exclude("net.kyori")
    }
    api("net.tnemc:TNIL-Paper:0.2.0.3-SNAPSHOT-1") {
        exclude("net.kyori")
    }
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).apply {
        windowTitle = "Quickshop - Hikari"
        addStringOption("Xdoclint:none", "-quiet")
        exclude(
            "**/com/ghostchu/quickshop/nonquickshopstuff/**",
            "**/com/ghostchu/quickshop/compatibility/**",
            "**/com/ghostchu/quickshop/addon/**",
        )
        author(true)
        version(true)
        linkSource(true)
        noTimestamp(true)
        bottom = "<b>QuickShopCommunity, 2026</b>"
    }
    isFailOnError = false
}
