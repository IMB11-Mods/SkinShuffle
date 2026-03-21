@file:Suppress("UnstableApiUsage")

plugins {
    id("net.fabricmc.fabric-loom")
    id("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["mod_version"] = prop("mod.version")
        this["minecraft_version"] = prop("deps.minecraft")
        this["target_minecraft"] = prop("mod.target")
        this["mod_id"] = "skinshuffle"
        this["mod_name"] = "Skin Shuffle"
        this["mod_description"] = "Choose and change your skin in-game!"
        this["mod_license"] = "ARR"
        this["target_yacl"] = "*"
        this["target_fabricloader"] = "0.18.0"
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
    accessWidenerPath = getRootProject().file("src/main/resources/skinshuffle.classtweaker")
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

    implementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    implementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")
    include("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")

    compileOnly("maven.modrinth:modmenu:${property("runtime.modmenu")}")
    runtimeOnly("com.terraformersmc:modmenu:${property("runtime.modmenu")}")

    compileOnly("maven.modrinth:minecraftcapes:${property("runtime.minecraftcapes")}")
    compileOnly("maven.modrinth:capes:${property("runtime.capes")}")
    compileOnly("maven.modrinth:entitytexturefeatures:${property("runtime.etf")}")

    implementation("com.konghq:unirest-java:3.11.09:standalone")
    include("com.konghq:unirest-java:3.11.09:standalone")

    implementation("dev.lambdaurora:spruceui:${property("deps.spruceui")}")
    include("dev.lambdaurora:spruceui:${property("deps.spruceui")}")
    include("dev.yumi.mc.core:yumi-mc-foundation:${property("deps.yumi_mc_foundation")}")

    implementation("org.jsoup:jsoup:1.16.1")
    include("org.jsoup:jsoup:1.16.1")

    implementation("commons-validator:commons-validator:1.7")
    include("commons-validator:commons-validator:1.7")

    implementation("com.drewnoakes:metadata-extractor:2.19.0")
    include("com.drewnoakes:metadata-extractor:2.19.0")

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
    runtimeOnly("net.fabricmc:fabric-language-kotlin:1.13.2+kotlin.2.1.20")
}

tasks {
    processResources {
        exclude("**/neoforge.mods.toml", "**/mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(jar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

java {
    withSourcesJar()
    val javaCompat = if (stonecutter.eval(stonecutter.current.version, ">26")) {
        JavaVersion.VERSION_25
    } else {
        JavaVersion.VERSION_21
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