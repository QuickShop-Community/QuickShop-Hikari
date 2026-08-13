package com.ghostchu.quickshop.buildlogic

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.time.format.DateTimeFormatter
import java.time.ZoneId

abstract class GitInfoValueSource : ValueSource<Map<String, String>, ValueSourceParameters.None> {

    private fun git(vararg args: String): String = try {
        val process = ProcessBuilder(listOf("git") + args)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        if (process.waitFor() == 0) output else ""
    } catch (e: Exception) {
        ""
    }

    override fun obtain(): Map<String, String> {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
        return mapOf(
            "git.tags" to git("tag", "--points-at", "HEAD"),
            "git.branch" to git("rev-parse", "--abbrev-ref", "HEAD"),
            "git.dirty" to git("status", "--porcelain").isNotEmpty().toString(),
            "git.remote.origin.url" to git("config", "--get", "remote.origin.url"),
            "git.commit.id" to git("rev-parse", "HEAD"),
            "git.commit.id.abbrev" to git("rev-parse", "--short", "HEAD"),
            "git.commit.user.name" to git("log", "-1", "--pretty=%an"),
            "git.commit.user.email" to git("log", "-1", "--pretty=%ae"),
            "git.commit.message.short" to git("log", "-1", "--pretty=%s"),
            "git.commit.time" to git("log", "-1", "--date=format:%Y-%m-%d %H:%M:%S", "--pretty=%cd"),
            "git.build.time" to dateFormat.format(java.time.Instant.now()),
            "git.build.user.name" to git("config", "user.name"),
            "git.build.user.email" to git("config", "user.email"),
        )
    }
}
