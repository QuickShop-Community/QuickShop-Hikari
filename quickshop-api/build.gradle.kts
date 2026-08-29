plugins {
    id("quickshop.core-conventions")
    `maven-publish`
}

dependencies {
    api(project(":quickshop-common"))

    compileOnly(libs.paper.api)

    api(libs.boosted.yaml) {
        exclude("org.jetbrains", "annotations-java5")
    }

    api(libs.tnil.bukkit) {
        exclude("net.kyori")
    }

    api(libs.tnil.paper) {
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = property("group") as String
            version = property("version") as String
            artifactId = "quickshop-api"
        }
    }
    repositories {
        maven {
            url = uri("https://repo.codemc.io/repository/ghost-chu/")

            val mavenUsername = System.getenv("GRADLE_PROJECT_MAVEN_USERNAME")
            val mavenPassword = System.getenv("GRADLE_PROJECT_MAVEN_PASSWORD")
            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
}