plugins {
    id("quickshop.core-conventions")
}

dependencies {
    api("org.apache.commons:commons-lang3:3.18.0")
    api("org.slf4j:slf4j-jdk14:2.0.17")
    api("com.google.code.gson:gson:2.13.1")
    api("com.ghostchu:simplereloadlib:1.1.2")
    api("cc.carm.lib:easysql-api:0.4.7")
    api("com.vdurmont:semver4j:3.1.0")

    compileOnly("net.kyori:adventure-api:5.1.1")
}
