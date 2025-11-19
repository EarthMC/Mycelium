plugins {
    id("com.gradleup.shadow") version "9.1.0" apply false
    alias(libs.plugins.conventions.java) apply false
    alias(libs.plugins.conventions.publishing) apply false
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "net.earthmc.conventions.java")
}
