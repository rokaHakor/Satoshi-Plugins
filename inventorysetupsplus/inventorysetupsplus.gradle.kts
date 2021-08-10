import ProjectVersions.rlVersion

version = "1.0.2"

project.extra["PluginName"] = "Inventory Setups Plus"
project.extra["PluginDescription"] =
    "Setups up your Inventory"

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)
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