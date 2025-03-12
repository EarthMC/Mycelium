plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

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

        shadowJar {
            archiveClassifier.set("")
        }
    }
}
