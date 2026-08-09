tasks.named<ProcessResources>("processResources") {
    val pluginArtifactId = if (project.extra.has("quickshopArtifactId")) {
        project.extra["quickshopArtifactId"] as String
    } else {
        project.name
    }
    filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
        expand(mapOf("project" to mapOf("version" to project.version, "artifactId" to pluginArtifactId)))
    }
}
