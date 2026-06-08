plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("hytaleBase") {
            id = "com.kukso.hytale.base"
            implementationClass = "com.kukso.gradle.HytaleBasePlugin"
        }
        register("hytaleLibrary") {
            id = "com.kukso.hytale.library"
            implementationClass = "com.kukso.gradle.HytaleLibraryPlugin"
        }
        register("hytaleMod") {
            id = "com.kukso.hytale.mod"
            implementationClass = "com.kukso.gradle.HytaleModPlugin"
        }
    }
}
