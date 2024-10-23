plugins {
    id("com.gradle.develocity") version("3.18.1")
}

rootProject.name = "ditto-wodt-adapter"

develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/help/legal-terms-of-use")
        termsOfUseAgree.set("yes")
        publishing.onlyIf { it.buildResult.failures.isNotEmpty() } // Publish the build scan when the build fails
    }
}
