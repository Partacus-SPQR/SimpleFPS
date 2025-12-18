plugins {
    id("fabric-loom")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
base.archivesName = property("mod.id") as String

// Note: Stonecutter handles source management via chiseledSrc
// Do NOT manually add root src directories - it causes duplicate class errors

repositories {
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:${property("deps.yarn")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    
    // Optional dependencies - Cloth Config
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modRuntimeOnly("me.shedaniel.cloth:cloth-config-fabric:${property("deps.cloth_config")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    
    // Optional dependencies - ModMenu
    modRuntimeOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    modCompileOnly("com.terraformersmc:modmenu:${property("deps.modmenu")}")
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"  // Shared run directory for all versions
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    processResources {
        // Handle duplicate resources from version-specific folders
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("minecraft", project.property("mod.mc_dep"))

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }
    
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
    
    compileJava {
        options.release = 21
    }
    
    jar {
        from("LICENSE") {
            rename { "${it}_${project.property("mod.id")}" }
        }
    }
}
