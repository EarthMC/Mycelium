plugins {
    id("java-library")
    alias(libs.plugins.conventions.publishing)
}

dependencies {
    api(project(":api"))
    api(libs.jedis)

    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.gson)
    compileOnly(libs.slf4j.api)
}
