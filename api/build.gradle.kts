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

    javadoc {
        val options = options as StandardJavadocDocletOptions

        options.use()
        options.links(
            "https://www.javadoc.io/doc/com.google.code.gson/gson/${libs.gson.get().version}/"
        )
    }
}
