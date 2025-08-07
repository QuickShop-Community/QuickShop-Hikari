plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    api(libs.org.apache.commons.commons.lang3)
    api(libs.org.slf4j.slf4j.jdk14)
    api(libs.com.google.code.gson.gson)
    api(libs.com.ghostchu.simplereloadlib)
    api(libs.cc.carm.lib.easysql.api)
    api(libs.net.kyori.adventure.text.serializer.ansi)
    api(libs.com.vdurmont.semver4j)
    compileOnly(libs.net.kyori.adventure.api)
    compileOnly(libs.net.kyori.adventure.key)
    compileOnly(libs.net.kyori.adventure.text.logger.slf4j)
    compileOnly(libs.net.kyori.adventure.text.minimessage)
    compileOnly(libs.net.kyori.adventure.text.serializer.json)
    compileOnly(libs.net.kyori.adventure.text.serializer.legacy)
    compileOnly(libs.net.kyori.adventure.text.serializer.plain)
    compileOnly(libs.net.kyori.examination.api)
    compileOnly(libs.net.kyori.examination.string)
}

description = "quickshop-common"
