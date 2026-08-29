plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api(libs.commons.lang3)
    api(libs.slf4j.jdk14)
    api(libs.gson)
    api(libs.simple.reload.lib)
    api(libs.easysql.api)
    api(libs.semver4j)

    compileOnly(libs.adventure.api)
}