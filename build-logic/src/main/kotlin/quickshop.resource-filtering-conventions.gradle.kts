tasks.named<ProcessResources>("processResources") {
    val pluginArtifactId = if (project.extra.has("quickshopArtifactId")) {
        project.extra["quickshopArtifactId"] as String
    } else {
        project.name
    }
    val pluginVersion = project.version.toString()
    filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
        expand(mapOf("project" to mapOf("version" to pluginVersion, "artifactId" to pluginArtifactId)))
    }
}
