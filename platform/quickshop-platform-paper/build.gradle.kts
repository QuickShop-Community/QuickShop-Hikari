plugins {
    id("quickshop.core-conventions")
}

dependencies {
    compileOnly(libs.paper.api.pinned)
    api(project(":platform:quickshop-platform-interface"))
}
