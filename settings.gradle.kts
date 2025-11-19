pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.earthmc.net/public")
    }
}

rootProject.name = "Mycelium"

include("api", "client", "examples", "platform:velocity", "platform:paper")
