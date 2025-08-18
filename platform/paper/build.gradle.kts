dependencies {
    implementation(project(":client"))
    compileOnly(libs.paper.api)
}

tasks {
    processResources {
        expand("version" to project.version)
    }
}
