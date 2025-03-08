plugins {
    `java-library`
}

dependencies {
    api(libs.jspecify)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
}
