val javaVersion: String = System.getProperty("java.version")
if (javaVersion.substringBefore(".").toInt() < 11) {
    throw GradleException("Java 11 or higher is required to build this project. You are using Java $javaVersion.")
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

rootProject.name = "Rocket Reserver"
include(":app")
