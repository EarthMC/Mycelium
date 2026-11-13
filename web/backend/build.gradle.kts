plugins {
    alias(libs.plugins.spring.boot)
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation(project(":platform:standalone"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
}

tasks {
    jar {
        // disable the plain (non shaded) jar
        enabled = false
    }
}
