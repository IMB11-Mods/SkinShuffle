plugins {
    id("net.neoforged.moddev")
    id ("dev.kikugie.postprocess.jsonlang")
    id("me.modmuss50.mod-publish-plugin")
}

tasks.named<ProcessResources>("processResources") {
    fun prop(name: String) = project.property(name) as String

    val props = HashMap<String, String>().apply {
        this["mod_version"] = prop("mod.version")
        this["target_minecraft"] = prop("mod.target")
        this["mod_id"] = "skinshuffle"
        this["mod_name"] = "Skin Shuffle"
        this["mod_description"] = "Choose and change your skin in-game!"
        this["mod_license"] = "ARR"
        this["target_yacl"] = "*"
        this["target_mru"] = "1.0.26"
        this["target_loader"] = "[4, )"
        this["loader"] = "neoforge"
    }

    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(props)
    }
}

version = "${property("mod.version")}+${property("deps.minecraft")}-neoforge"
base.archivesName = property("mod.id") as String

jsonlang {
    languageDirectories = listOf("assets/${property("mod.id")}/lang")
    prettyPrint = true
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

neoForge {
    enable {
        version = property("deps.neoforge") as String
        // Disable recompilation if the "CI" environment variable is set to true. It is automatically set by GitHub Actions.
        isDisableRecompilation = System.getenv("CI") == "true"
    }
    validateAccessTransformers = true

    runs {
        register("client") {
            gameDirectory = file("run/")
            client()
        }
        register("server") {
            gameDirectory = file("run/")
            server()
        }
    }

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
    sourceSets["main"].resources.srcDir("src/main/generated")
}

dependencies {

    implementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")
    jarJar("dev.isxander:yet-another-config-lib:${property("deps.yacl")}")

    compileOnly("maven.modrinth:minecraftcapes:${property("runtime.minecraftcapes")}")
    compileOnly("maven.modrinth:capes:${property("runtime.capes")}")
    compileOnly("maven.modrinth:entitytexturefeatures:${property("runtime.etf")}")

    implementation("com.konghq:unirest-java:3.11.09:standalone")
    jarJar("com.konghq:unirest-java:3.11.09:standalone")

    implementation("dev.lambdaurora:spruceui:${property("deps.spruceui")}") {
        isTransitive = false
    }
    jarJar("dev.lambdaurora:spruceui:${property("deps.spruceui")}") {
        isTransitive = false
    }
    implementation("dev.yumi.mc.core:yumi-mc-foundation:${property("deps.yumi_mc_foundation")}")
    jarJar("dev.yumi.mc.core:yumi-mc-foundation:${property("deps.yumi_mc_foundation")}")

    implementation("org.jsoup:jsoup:1.16.1")
    jarJar("org.jsoup:jsoup:1.16.1")

    implementation("commons-validator:commons-validator:1.7")
    jarJar("commons-validator:commons-validator:1.7")

    implementation("com.drewnoakes:metadata-extractor:2.19.0")
    jarJar("com.drewnoakes:metadata-extractor:2.19.0")

    runtimeOnly("me.djtheredstoner:DevAuth-neoforge:1.2.1")
}

tasks.named("processResources") {
    dependsOn(":${stonecutter.current.project}:stonecutterGenerate")
}

tasks {
    processResources {
        exclude("**/fabric.mod.json", "**/*.accesswidener", "**/mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
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
