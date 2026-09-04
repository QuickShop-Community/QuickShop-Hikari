plugins {
    alias(libs.plugins.versions)
}

allprojects {
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

tasks.register<Copy>("collectReleaseArtifacts") {
    dependsOn(
        subprojects.map { subproject ->
            subproject.tasks.matching { it.name == "build" }
        }
    )

    into(layout.buildDirectory.dir("release"))

    subprojects.forEach { subproject ->
        from(subproject.layout.buildDirectory.dir("libs")) {
            include("*.jar")
            exclude("*-sources.jar")
            exclude("*-javadoc.jar")
        }
    }

    duplicatesStrategy = DuplicatesStrategy.FAIL
}