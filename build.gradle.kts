plugins {
    `java`
    id("buildlogic.java-conventions")
    id("com.gradleup.shadow") version "9.0.0-beta16" apply true
}

group = "com.ghostchu.quickshop"
version = "6.2.0.11-SNAPSHOT-4"
description = "The QuickShop Fork Pushing the Boundaries."
val relocation = "com.ghostchu.quickshop.shade"

repositories {
    mavenCentral()
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly(libs.annotations.jetbrains)
}

subprojects {

    apply(plugin = "java")

    java {
        withSourcesJar()
        withJavadocJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks {

        compileJava {
            options.encoding = "UTF-8"
            options.release.set(21)

            sourceCompatibility = "21"
            targetCompatibility = "21"
        }

        jar {
            from("../LICENSE")
        }
    }
}

//TODO: Publishing to modrinth, etc

publishing {
    publications.create<MavenPublication>("shadow") {
        from(components["shadow"])

        pom {
            name = rootProject.name
            description = "QuickShop Hikari is a QuickShop fork for Modern Minecraft servers."
            url = "https://modrinth.com/plugin/quickshop-hikari"
            organization {
                name = "QuickShop Community"
                url = "https://github.com/QuickShop-Community"
            }
            licenses {
                license {
                    name = "GNU AFFERO General Public License, Version 3 (AGPL-3.0)"
                    url = "https://github.com/QuickShop-Community/QuickShop-Hikari/blob/hikari/LICENSE"
                }
            }
            developers {
                developer {
                    id = "creatorfromhell"
                    name = "Daniel \"creatorfromhell\" Vidmar"
                    email = "daniel.viddy@gmail.com"
                    url = "https://cfh.dev"
                    organization = "The New Economy"
                    organizationUrl = "https://tnemc.net"
                }
                developer {
                    id = "Ghost_chu"
                    name = "Ghost chu"
                    email = "2908803755@qq.com"
                    url = "https://www.ghostchu.com"
                }
            }
            scm {
                connection = "scm:git:git://github.com/QuickShop-Community/QuickShop-Hikari.git"
                developerConnection = "scm:git:git://github.com/QuickShop-Community/QuickShop-Hikari.git"
                url = "https://github.com/QuickShop-Community/QuickShop-Hikari"
            }
            issueManagement {
                system = "GitHub"
                url = "https://github.com/QuickShop-Community/QuickShop-Hikari/issues"
            }
        }
    }

    repositories {
        maven {
            name = "CodeMC"
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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
    isFailOnError = false

    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        windowTitle = "QuickShop-Hikari"
        isAuthor = true
        isVersion = true
        links ("https://docs.oracle.com/javase/21/docs/api/", "")
        bottom = "<b>creatorfromhell, 2025</b>"
        isNoTimestamp = true
    }
}