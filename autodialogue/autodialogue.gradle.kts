version = "1.0.2"

project.extra["PluginName"] = "Auto Dialogue"
project.extra["PluginDescription"] = "Automates Dialogues"

dependencies {
    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.1.0")
    }
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    configuration("proguard.conf")
    injars("build/libs/autodialogue-$version.jar")
    outjars("build/libs/autodialogue2-$version.jar")

    target("11")

    adaptresourcefilenames()
    adaptresourcefilecontents()
    optimizationpasses(9)
    allowaccessmodification()
    mergeinterfacesaggressively()
    renamesourcefileattribute("SourceFile")
    keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

    libraryjars(System.getProperty("java.home") + "/jmods")
    libraryjars(configurations.compileClasspath.get())

}

tasks.register<Delete>("delete") {
    delete("build/libs/autodialogue-$version.jar")
}

tasks.register<Copy>("rename") {
    from("build/libs/")
    into("build/libs/")
    rename("autodialogue2-$version.jar", "autodialogue-$version.jar")
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