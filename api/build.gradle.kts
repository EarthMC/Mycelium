plugins {
    id("java-library")
    alias(libs.plugins.conventions.publishing)
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
            "https://www.javadocs.dev/com.google.code.gson/gson/${libs.gson.get().version}/"
        )

        options.tags("apiNote", "implNote")
    }
}

earthmc {
    publishing {
        public = true
    }
}
