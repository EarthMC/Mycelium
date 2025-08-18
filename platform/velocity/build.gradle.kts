dependencies {
    implementation(project(":client"))

    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
}

tasks {
    shadowJar {
        dependsOn(project(":client").tasks.shadowJar)
    }
}
