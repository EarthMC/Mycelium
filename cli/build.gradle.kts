plugins {
    application
}

dependencies {
    implementation(project(":client"))

    implementation(libs.log4j)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j)
    implementation(libs.log4j.api)
}

application {
    mainClass = "net.earthmc.mycelium.cli.MyceliumCLI"
}

tasks {
    shadowJar {
        dependsOn(project(":client").tasks.shadowJar)
    }
}
