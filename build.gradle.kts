plugins {
    id("java")
    id("com.gradleup.shadow") version "9.1.0" apply false
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.release.set(21)
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name()
        }
    }
}
