plugins {
    alias(libs.plugins.versions)
}

allprojects {
    version = "6.3.0.1"

    plugins.withId("java") {
        configurations.all {
            resolutionStrategy {
                force("org.jetbrains:annotations:26.0.2-1")
            }
        }
    }
}

tasks.register("printVersion") {
    val printedVersion = project.version.toString()
    doLast { println(printedVersion) }
}
