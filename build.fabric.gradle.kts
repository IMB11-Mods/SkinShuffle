@file:Suppress("UnstableApiUsage")

plugins {
    id("fabric-loom")
    id("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["mod_version"] = prop("mod.version")
        this["minecraft_version"] = prop("deps.minecraft")
        this["target_minecraft"] = prop("mod.target")
        this["mod_id"] = "sounds"
        this["mod_name"] = "Sounds"
        this["mod_description"] = "A highly configurable sound overhaul mod that adds new sound effects while improving vanilla sounds too."
        this["mod_license"] = "ARR"
        this["target_yacl"] = "*"
        this["target_fabricloader"] = "0.17.2"
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("deps.minecraft")}-fabric"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
}


loom {
    accessWidenerPath = getRootProject().file("src/main/resources/aw/" + stonecutter.current.version + ".accesswidener")
}

stonecutter {
    replacements.string {
        direction = eval(current.version, ">1.21.10")
        replace("ResourceLocation", "Identifier")
    }
    replacements.string {
        direction = eval(current.version, ">1.21.10")
        replace("net.minecraft.Util", "net.minecraft.util.Util")
    }
    replacements.string {
        direction = eval(current.version, ">1.21.10")
        replace("ResourceKey::location", "ResourceKey::identifier")
    }
}

repositories {
    mavenLocal()
    maven {
        name = "Terraformers (Mod Menu)"
        url = uri("https://maven.terraformersmc.com/releases/")
        content {
            includeGroupAndSubgroups("com.terraformersmc")
        }
    }
    maven {
        name = "Xander Maven"
        url = uri("https://maven.isxander.dev/releases")
        content {
            includeGroupAndSubgroups("dev.isxander")
            includeGroupAndSubgroups("org.quiltmc.parsers")
        }
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroupAndSubgroups("maven.modrinth")
        }
    }
    maven {
        name = "gegy"
        url = uri("https://maven.gegy.dev")
        content {
            includeGroupAndSubgroups("dev.yumi")
            includeGroupAndSubgroups("dev.lambdaurora")
        }
    }
    maven {
        name = "DevAuth"
        url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
        content {
            includeGroupAndSubgroups("me.djtheredstoner")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("deps.minecraft")}")
    mappings(loom.layered {
        officialMojangMappings()
        if (hasProperty("deps.parchment"))
            parchment("org.parchmentmc.data:parchment-${property("deps.parchment")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")
    include("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")

    modCompileOnly("maven.modrinth:modmenu:${property("runtime.modmenu")}")
    modRuntimeOnly("com.terraformersmc:modmenu:${property("runtime.modmenu")}")

    modCompileOnly("maven.modrinth:minecraftcapes:${property("runtime.minecraftcapes")}")
    modCompileOnly("maven.modrinth:capes:${property("runtime.capes")}")
    modCompileOnly("maven.modrinth:entitytexturefeatures:${property("runtime.etf")}")

    implementation("com.konghq:unirest-java:3.11.09:standalone")
    include("com.konghq:unirest-java:3.11.09:standalone")

    modImplementation("dev.lambdaurora:spruceui:${property("deps.spruceui")}")
    include("dev.lambdaurora:spruceui:${property("deps.spruceui")}")
    include("dev.yumi.mc.core:yumi-mc-foundation:1.0.0-alpha.16+1.21.1")

    implementation("org.jsoup:jsoup:1.16.1")
    include("org.jsoup:jsoup:1.16.1")

    implementation("commons-validator:commons-validator:1.7")
    include("commons-validator:commons-validator:1.7")

    implementation("com.drewnoakes:metadata-extractor:2.19.0")
    include("com.drewnoakes:metadata-extractor:2.19.0")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
    modRuntimeOnly("net.fabricmc:fabric-language-kotlin:1.13.2+kotlin.2.1.20")
    
    val modules = listOf("transitive-access-wideners-v1", "registry-sync-v0", "resource-loader-v0")
    for (it in modules) modImplementation(fabricApi.module("fabric-$it", property("deps.fabric_api") as String))
}

fabricApi {
    configureDataGeneration() {
        outputDirectory = file("$rootDir/src/main/generated")
        client = true
    }
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    withSourcesJar()
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, ">=1.21")) {
        JavaVersion.VERSION_21
    } else {
        JavaVersion.VERSION_17
    }
    sourceCompatibility = javaCompat
    targetCompatibility = javaCompat
}

val additionalVersionsStr = findProperty("publish.additionalVersions") as String?
        val additionalVersions: List<String> = additionalVersionsStr
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()