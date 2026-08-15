import com.ghostchu.quickshop.buildlogic.StripEmbeddedJetbrainsAnnotations
import org.gradle.api.attributes.Attribute

plugins {
    id("java-library")
}

val artifactType = Attribute.of("artifactType", String::class.java)

dependencies {
    registerTransform(StripEmbeddedJetbrainsAnnotations::class) {
        from.attribute(artifactType, "jar")
        to.attribute(artifactType, "jar-annotations-stripped")
    }
}

configurations.named("compileClasspath") {
    attributes.attribute(artifactType, "jar-annotations-stripped")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2-1")
}
