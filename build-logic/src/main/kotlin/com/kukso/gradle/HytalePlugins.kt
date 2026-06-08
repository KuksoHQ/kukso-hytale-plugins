package com.kukso.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import java.io.File
import java.util.Locale

class HytaleBasePlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply("java-library")

        group = findProperty("group") as String? ?: "com.kukso.hytale.$name"
        version = projectDir.resolve("version.txt").takeIf { it.exists() }?.readText()?.trim().orEmpty()
            .ifBlank { "0.0.0-SNAPSHOT" }

        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        }

        tasks.withType<Jar>().configureEach {
            archiveBaseName.set("${kuksoProductName()}-Hytale")
            archiveVersion.set(version.toString())
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(25)
        }

        tasks.withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
        }

        val home = findProperty("hytale_home") as String?
            ?: findProperty("hytaleHome") as String?
            ?: System.getenv("HYTALE_HOME")
            ?: defaultHytaleHome()
        val patchline = findProperty("patchline") as String? ?: "release"
        val gameBuild = findProperty("game_build") as String? ?: "2026.03.26-89796e57b"
        val serverJar = File(home, "install/$patchline/package/game/$gameBuild/Server/HytaleServer.jar")
        if (!serverJar.exists()) {
            throw org.gradle.api.GradleException(
                "Hytale server jar was not found at ${serverJar.absolutePath}. " +
                    "Install Hytale locally or set HYTALE_HOME, hytaleHome, or hytale_home to the Hytale home directory. " +
                    "You can also override patchline and game_build."
            )
        }
        dependencies.add("compileOnly", files(serverJar))

        tasks.withType<Copy>().matching { it.name == "processResources" }.configureEach {
            filteringCharset = "UTF-8"
            val props = mapOf(
                "mod_description" to (findProperty("mod_description") ?: ""),
                "website" to (findProperty("website") ?: "https://kukso.com"),
                "server_version" to (findProperty("server_version") ?: "2026.03.26-89796e57b"),
                "entry_point" to (findProperty("entry_point") ?: ""),
                "includes_pack" to (findProperty("includes_pack") ?: "true"),
                "mod_name" to (findProperty("mod_name") ?: name),
                "group" to group.toString(),
                "version" to version.toString()
            )
            inputs.properties(props)
            filesMatching("manifest.json") {
                expand(props)
            }
            doLast {
                outputs.files.asFileTree.matching { include("manifest.json") }.forEach {
                    it.writeText(
                        it.readText()
                            .replace("\"IncludesAssetPack\": \"true\"", "\"IncludesAssetPack\": true")
                            .replace("\"IncludesAssetPack\": \"false\"", "\"IncludesAssetPack\": false")
                    )
                }
            }
        }
    }

    private fun defaultHytaleHome(): String {
        val home = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        return when {
            os.contains("win") -> "$home/AppData/Roaming/Hytale"
            os.contains("mac") -> "$home/Library/Application Support/Hytale"
            else -> "$home/.local/share/Hytale"
        }
    }

    private fun Project.kuksoProductName(): String {
        return (findProperty("mod_name") as String?) ?: when (name) {
            "lib" -> "KuksoLib"
            "econ" -> "KuksoEcon"
            "warps" -> "KuksoWarps"
            else -> "Kukso${name.replaceFirstChar { it.uppercase() }}"
        }
    }
}

class HytaleLibraryPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.apply(HytaleBasePlugin::class.java)

        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

class HytaleModPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            pluginManager.apply(HytaleBasePlugin::class.java)
        }
    }
}
