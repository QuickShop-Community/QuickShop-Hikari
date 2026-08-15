package com.ghostchu.quickshop.buildlogic

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

abstract class StripEmbeddedJetbrainsAnnotations : TransformAction<TransformParameters.None> {

    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        if (!input.name.startsWith("boosted-yaml")) {
            outputs.file(input)
            return
        }
        val output = outputs.file(input.name)
        ZipInputStream(input.inputStream().buffered()).use { zin ->
            ZipOutputStream(output.outputStream().buffered()).use { zout ->
                var entry: ZipEntry? = zin.nextEntry
                while (entry != null) {
                    if (!entry.name.startsWith("org/jetbrains/annotations/")) {
                        zout.putNextEntry(ZipEntry(entry.name))
                        zin.copyTo(zout)
                        zout.closeEntry()
                    }
                    zin.closeEntry()
                    entry = zin.nextEntry
                }
            }
        }
    }
}
