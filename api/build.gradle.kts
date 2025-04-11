plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api(libs.jspecify)
    api(libs.jetbrains.annotations)
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
        }
    }
}
