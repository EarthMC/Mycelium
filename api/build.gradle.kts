plugins {
    `java-library`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(libs.jspecify)
    api(libs.jetbrains.annotations)
    api(libs.concurrentutil)
    compileOnlyApi(libs.gson)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
}
