plugins {
    id("com.gradleup.shadow")
    id("java-library")
    alias(libs.plugins.conventions.publishing)
}

dependencies {
    api(project(":client"))

    api(libs.adventure.api)
    api(libs.adventure.serializer.gson)
}

earthmc {
    publishing {
        public = true
        artifactId = "standalone"
    }
}
