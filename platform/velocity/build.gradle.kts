plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":client"))

    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    compileJava {
        dependsOn(generateTemplates)
    }

    shadowJar {
        archiveBaseName.set("MyceliumVelocity")
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
}

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(file("src/main/templates"))
    into(layout.buildDirectory.dir("generated/sources/templates"))
    expand(props)
}

java.sourceSets["main"].java.srcDir(generateTemplates.map { it.outputs })
