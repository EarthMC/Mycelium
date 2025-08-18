plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":client"))
    compileOnly(libs.paper.api)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("MyceliumPaper")
        archiveClassifier.set("")

        relocate("redis.clients", "net.earthmc.mycelium.libs.redis")
        relocate("org.apache.commons.pool2", "net.earthmc.mycelium.libs.pool2")
        relocate("org.json", "net.earthmc.mycelium.libs.json")

        dependencies {
            exclude(dependency("com.google.code.gson:gson"))
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("com.google.errorprone:error_prone_annotations"))
        }
    }

    processResources {
        expand("version" to project.version)
    }
}
