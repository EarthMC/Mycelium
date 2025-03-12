plugins {
    `java-library`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(project(":api"))
    api(libs.lettuce)

    implementation(libs.gson)

    // todo: sort out dependencies for platforms that already have these
    implementation(libs.log4j)
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j)
    implementation(libs.log4j.api)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        dependsOn(project(":api").tasks.shadowJar)
    }
}
