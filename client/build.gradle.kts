plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    api(libs.lettuce)

    implementation(libs.gson)

    compileOnly(libs.slf4j.api)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        dependsOn(project(":api").tasks.shadowJar)
    }
}
