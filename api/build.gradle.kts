plugins {
    id("java-library")
    id("maven-publish")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.gson)
}

tasks {
    javadoc {
        val options = options as StandardJavadocDocletOptions

        options.use()
        options.links(
            "https://www.javadoc.io/doc/com.google.code.gson/gson/${libs.gson.get().version}/"
        )

        options.tags("apiNote", "implNote")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            val releasesUrl = "https://repo.earthmc.net/releases"
            val snapshotsUrl = "https://repo.earthmc.net/snapshots"
            url = uri(if (project.version.toString().endsWith("-SNAPSHOT")) snapshotsUrl else releasesUrl)

            name = "earthmc"
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("library") {
            from(components.getByName("java"))

            artifactId = "mycelium-api"
        }
    }
}
