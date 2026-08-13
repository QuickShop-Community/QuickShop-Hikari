plugins {
    id("quickshop.core-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    api(project(":quickshop-common"))
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.15.0") {
        isTransitive = false
    }
}
