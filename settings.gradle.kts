pluginManagement {
    repositories {
        maven { url=uri ("https://maven.aliyun.com/repository/public/") }
        maven { url=uri ("https://maven.aliyun.com/repository/google/") }
        maven { url=uri ("https://maven.aliyun.com/repository/gradle-plugin/") }
        maven { url=uri( "https://jitpack.io") } // 必须添加，且建议放在最后

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "UnSunon"
include(":app")
