plugins {
    id("quickshop.compat-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":compatibility:common"))
    compileOnly(project(":quickshop-bukkit"))
    compileOnly("com.github.Xyness:SimpleClaimSystem:1.11.6.1")
}

configurations.named("compileClasspath") {
    attributes {
        attribute(org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 22)
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveBaseName.set("Compat-SimpleClaimSystem")
}
