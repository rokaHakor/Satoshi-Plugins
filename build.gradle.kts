import ProjectVersions.rlVersion

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    java
    id("com.github.ben-manes.versions") version "0.36.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.15"
    id("com.simonharrer.modernizer") version "2.1.0-1" apply false
}

project.extra["GithubUrl"] = "https://github.com/rokaHakor/Satoshi-Plugins"
apply<BootstrapPlugin>()
apply<VersionPlugin>()

allprojects {
    group = "com.openosrs.externals"
    apply<MavenPublishPlugin>()
}

subprojects {
    var subprojectName = name
    group = "com.openosrs.externals"

    project.extra["PluginProvider"] = "Satoshi Oda"
    project.extra["ProjectUrl"] = "https://github.com/rokaHakor/Satoshi-Plugins"
    project.extra["ProjectSupportUrl"] = "https://discord.gg/qPrKNJvBK5"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    repositories {
        jcenter {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
                excludeGroupByRegex("com\\.runelite.*")
            }
        }

        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://repo.runelite.net")
                }
            }
            filter {
                includeModule("net.runelite", "discord")
                includeModule("net.runelite.jogl", "jogl-all")
                includeModule("net.runelite.gluegen", "gluegen-rt")
            }
        }

        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            filter {
                includeGroupByRegex("com\\.openosrs.*")
            }
        }
    }

    apply<JavaPlugin>()
    apply(plugin = "com.github.ben-manes.versions")
    apply(plugin = "se.patrikerdes.use-latest-versions")
    apply(plugin = "com.simonharrer.modernizer")

    dependencies {
        compileOnly("com.openosrs:runelite-api:$rlVersion")
        compileOnly("com.openosrs:runelite-client:$rlVersion")
        compileOnly("com.openosrs:http-api:$rlVersion")
        compileOnly("com.openosrs.rs:runescape-api:$rlVersion")

        compileOnly(Libraries.okhttp3)
        compileOnly(Libraries.guice)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.pf4j)
        compileOnly(Libraries.apacheCommonsText)
        compileOnly(Libraries.gson)
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("$buildDir/repo")
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }

//        withType<Jar> {
//            doLast {
//                val externalManagerDirectory: String = project.findProperty("externalManagerDirectory")?.toString()
//                    ?: System.getProperty("user.home") + "\\.openosrs\\plugins"
//                copy {
//                    from("./build/libs/")
//                    into(externalManagerDirectory)
//                }
//            }
//        }

        named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
            checkForGradleUpdate = false

            resolutionStrategy {
                componentSelection {
                    all {
                        if (candidate.displayName.contains("fernflower") || isNonStable(candidate.version)) {
                            reject("Non stable")
                        }
                    }
                }
            }
        }

        register<Copy>("copyDeps") {
            into("./build/deps/")
            from(configurations["runtimeClasspath"])
        }
    }
}

tasks {
    named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates") {
        checkForGradleUpdate = false

        resolutionStrategy {
            componentSelection {
                all {
                    if (candidate.displayName.contains("fernflower") || isNonStable(candidate.version)) {
                        reject("Non stable")
                    }
                }
            }
        }
    }
}

fun isNonStable(version: String): Boolean {
    return listOf("ALPHA", "BETA", "RC").any {
        version.toUpperCase().contains(it)
    }
}
