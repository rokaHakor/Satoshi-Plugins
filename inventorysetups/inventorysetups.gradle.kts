import ProjectVersions.rlVersion

version = "1.0.1"

project.extra["PluginName"] = "Inventory Setups"
project.extra["PluginDescription"] =
    "Setups up your Inventory"

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)

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

tasks {
    jar {
        manifest {
            attributes(
                mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
                )
            )
        }
    }
}