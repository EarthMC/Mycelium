plugins {
    application
}

dependencies {
    implementation(project(":client"))
    implementation(project(":api"))

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

    startScripts {
        dependsOn(project(":client").tasks.shadowJar)
        dependsOn(shadowJar)
    }

    distZip {
        dependsOn(shadowJar)
    }

    distTar {
        dependsOn(shadowJar)
    }

    startShadowScripts {
        dependsOn(jar)
    }
}
