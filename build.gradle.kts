plugins {
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.141"
    id("idea")
}

val modId = providers.gradleProperty("mod_id").get()
val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val minecraftVersionRange = providers.gradleProperty("minecraft_version_range").get()
val neoVersion = providers.gradleProperty("neo_version").get()
val neoVersionRange = providers.gradleProperty("neo_version_range").get()
val loaderVersionRange = providers.gradleProperty("loader_version_range").get()
val modLicense = providers.gradleProperty("mod_license").get()
val modName = providers.gradleProperty("mod_name").get()
val modAuthors = providers.gradleProperty("mod_authors").get()
val modDescription = providers.gradleProperty("mod_description").get()

version = "${providers.gradleProperty("mod_version").get()}+mc$minecraftVersion-neoforge"
group = providers.gradleProperty("maven_group").get()

base {
    archivesName.set(providers.gradleProperty("archives_base_name").get())
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

sourceSets {
    main {
        java {
            srcDir("src/compat/legacy/base/java")
            srcDir("src/compat/legacy/useitem/typed/java")
            srcDir("src/compat/legacy/render/old/java")
        }
        resources {
            exclude("fabric.mod.json")
            exclude("pathmind.accesswidener")
        }
    }
}

neoForge {
    version = neoVersion

    parchment {
        minecraftVersion = providers.gradleProperty("parchment_minecraft_version").get()
        mappingsVersion = providers.gradleProperty("parchment_mappings_version").get()
    }

    runs {
        configureEach {
            gameDirectory.set(layout.projectDirectory.dir("run"))
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel.set(org.slf4j.event.Level.DEBUG)
        }

        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }
    }

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    unitTest {
        enable()
        testedMod = mods.named(modId).get()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "neo_version" to neoVersion,
        "neo_version_range" to neoVersionRange,
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_license" to modLicense,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription
    )
    inputs.properties(properties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
    options.compilerArgs.addAll(listOf("-Xlint:-deprecation", "-Xlint:-removal"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
