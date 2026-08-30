plugins {
    id("quickshop.core-conventions")
}

dependencies {
    compileOnly(libs.paper.api)
    api(project(":quickshop-common"))
    compileOnly(libs.item.nbt.api.plugin) {
        isTransitive = false
    }
}
