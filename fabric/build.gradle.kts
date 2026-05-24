import org.gradle.api.GradleException

plugins {
    id("fabric-loom") version "1.14.10"
    id("maven-publish")
}

val modId = providers.gradleProperty("mod_id").get()
data class MinecraftVersionSpec(
    val fabricApiVersion: String,
    val architecturyApiVersion: String
)

val supportedMinecraftVersions = linkedMapOf(
    "1.21" to MinecraftVersionSpec("0.102.0+1.21", "13.0.8"),
    "1.21.1" to MinecraftVersionSpec("0.116.7+1.21.1", "13.0.8"),
    "1.21.2" to MinecraftVersionSpec("0.106.1+1.21.2", "14.0.4"),
    "1.21.3" to MinecraftVersionSpec("0.114.1+1.21.3", "14.0.4"),
    "1.21.4" to MinecraftVersionSpec("0.119.4+1.21.4", "15.0.3"),
    "1.21.5" to MinecraftVersionSpec("0.128.2+1.21.5", "16.1.4"),
    "1.21.6" to MinecraftVersionSpec("0.128.2+1.21.6", "17.0.8"),
    "1.21.7" to MinecraftVersionSpec("0.129.0+1.21.7", "17.0.8"),
    "1.21.8" to MinecraftVersionSpec("0.133.4+1.21.8", "18.0.8"),
    "1.21.9" to MinecraftVersionSpec("0.134.1+1.21.9", "19.0.1"),
    "1.21.10" to MinecraftVersionSpec("0.138.4+1.21.10", "19.0.1"),
    "1.21.11" to MinecraftVersionSpec("0.140.2+1.21.11", "19.0.1")
)

val explicitMinecraftVersion = providers.gradleProperty("mc_version").isPresent
val minecraftVersion = providers.gradleProperty("mc_version")
    .orElse(providers.gradleProperty("minecraft_version"))
    .get()
val requestedSpec = supportedMinecraftVersions[minecraftVersion]
    ?: throw GradleException("No Fabric build spec configured for Minecraft $minecraftVersion")
val fabricLoaderVersion = providers.gradleProperty("fabric_loader_version").get()
val fabricApiVersion = if (explicitMinecraftVersion) {
    requestedSpec.fabricApiVersion
} else {
    providers.gradleProperty("fabric_api_version")
        .orElse(provider { requestedSpec.fabricApiVersion })
        .get()
}
val architecturyApiVersion = if (explicitMinecraftVersion) {
    requestedSpec.architecturyApiVersion
} else {
    providers.gradleProperty("architectury_api_version")
        .orElse(provider { requestedSpec.architecturyApiVersion })
        .get()
}
val modVersion = providers.gradleProperty("mod_version").get()
val usesGeneratedCompatibilitySources = minecraftVersion in setOf("1.21.9", "1.21.10", "1.21.11")
val usesRenamedMojangApis = minecraftVersion == "1.21.11"
val keyMappingCategoryIdFactory = if (usesRenamedMojangApis) {
    "net.minecraft.resources.Identifier.fromNamespaceAndPath"
} else {
    "net.minecraft.resources.ResourceLocation.fromNamespaceAndPath"
}
val renamedMojangApiReplacements = buildList {
    if (usesRenamedMojangApis) {
        add("net.minecraft.resources.ResourceLocation" to "net.minecraft.resources.Identifier")
        add("ResourceLocation" to "Identifier")
        add("net.minecraft.Util" to "net.minecraft.util.Util")
        add("net.minecraft.world.entity.npc.Villager" to "net.minecraft.world.entity.npc.villager.Villager")
        add("net.minecraft.world.entity.npc.VillagerData" to "net.minecraft.world.entity.npc.villager.VillagerData")
        add("net.minecraft.world.entity.npc.VillagerProfession" to "net.minecraft.world.entity.npc.villager.VillagerProfession")
        add("net.minecraft.world.entity.npc.VillagerTrades" to "net.minecraft.world.entity.npc.villager.VillagerTrades")
        add("net.minecraft.world.entity.npc.VillagerType" to "net.minecraft.world.entity.npc.villager.VillagerType")
        add("net.minecraft.world.entity.MobSpawnType" to "net.minecraft.world.entity.EntitySpawnReason")
        add("MobSpawnType" to "EntitySpawnReason")
        add("net.minecraft.client.renderer.RenderType" to "net.minecraft.client.renderer.rendertype.RenderType")
        add("protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)" to "protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta)")
        add("RenderType.lines()" to "net.minecraft.client.renderer.rendertype.RenderTypes.lines()")
        add(".location()" to ".identifier()")
    }
    add("minecraft.screen.renderWithTooltip(context, mouseX, mouseY, delta)" to "minecraft.screen.renderWithTooltipAndSubtitles(context, mouseX, mouseY, delta)")
    add("client.getWindow().getWindow()" to "client.getWindow().handle()")
    add("window.getWindow()" to "window.handle()")
    add("    public static KeyMapping STOP_GRAPHS;" to "    public static KeyMapping STOP_GRAPHS;\n    private static final KeyMapping.Category GENERAL_CATEGORY = KeyMapping.Category.register($keyMappingCategoryIdFactory(\"pathmind\", \"general\"));")
    add("\"category.pathmind.general\"" to "GENERAL_CATEGORY")
    add("public boolean mouseClicked(double mouseXDouble, double mouseYDouble, int button) {" to "public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean inBounds) {\n        double mouseXDouble = click.x();\n        double mouseYDouble = click.y();\n        int button = click.button();")
    add("public boolean mouseClicked(double mouseX, double mouseY, int button) {" to "public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean inBounds) {\n        double mouseX = click.x();\n        double mouseY = click.y();\n        int button = click.button();")
    add("public boolean mouseDragged(double mouseXDouble, double mouseYDouble, int button, double deltaX, double deltaY) {" to "public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click, double deltaX, double deltaY) {\n        double mouseXDouble = click.x();\n        double mouseYDouble = click.y();\n        int button = click.button();")
    add("public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {" to "public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click, double deltaX, double deltaY) {\n        double mouseX = click.x();\n        double mouseY = click.y();\n        int button = click.button();")
    add("public boolean mouseReleased(double mouseX, double mouseY, int button) {" to "public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent click) {\n        double mouseX = click.x();\n        double mouseY = click.y();\n        int button = click.button();")
    add("public boolean keyPressed(int keyCode, int scanCode, int modifiers) {" to "public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {\n        int keyCode = input.key();\n        int scanCode = input.scancode();\n        int modifiers = input.modifiers();")
    add("public boolean charTyped(char chr, int modifiers) {" to "public boolean charTyped(net.minecraft.client.input.CharacterEvent input) {\n        char chr = (char) input.codepoint();\n        int modifiers = input.modifiers();")
    add("return super.mouseClicked(mouseXDouble, mouseYDouble, button);" to "return super.mouseClicked(click, inBounds);")
    add("return super.mouseClicked(mouseX, mouseY, button);" to "return super.mouseClicked(click, inBounds);")
    add("return super.mouseDragged(mouseXDouble, mouseYDouble, button, deltaX, deltaY);" to "return super.mouseDragged(click, deltaX, deltaY);")
    add("return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);" to "return super.mouseDragged(click, deltaX, deltaY);")
    add("return super.mouseReleased(mouseX, mouseY, button);" to "return super.mouseReleased(click);")
    add("return super.keyPressed(keyCode, scanCode, modifiers);" to "return super.keyPressed(input);")
    add("return super.charTyped(chr, modifiers);" to "return super.charTyped(input);")
    add("ScreenKeyboardEvents.afterKeyPress(screen).register((currentScreen, keyCode, scanCode, modifiers) -> {" to "ScreenKeyboardEvents.afterKeyPress(screen).register((currentScreen, input) -> {\n            int keyCode = input.key();")
    add("import net.minecraft.world.InteractionResultHolder;" to "import net.minecraft.world.InteractionResult;")
    add("InteractionResultHolder.pass(player.getItemInHand(hand))" to "InteractionResult.PASS")
}
fun transformRenamedMojangLine(line: String): String {
    var current = renamedMojangApiReplacements.fold(line) { value, replacement ->
        value.replace(replacement.first, replacement.second)
    }
    current = Regex("""(?<![A-Za-z0-9_])(?!(?:super)\b)([A-Za-z_][A-Za-z0-9_.]*)\.mouseClicked\(([^,]+),\s*([^,]+),\s*button\)""").replace(current) {
        "com.pathmind.util.InputCompatibilityBridge.mouseClicked(${it.groupValues[1]}, ${it.groupValues[2]}, ${it.groupValues[3]}, button)"
    }
    current = Regex("""(?<![A-Za-z0-9_])(?!(?:super)\b)([A-Za-z_][A-Za-z0-9_.]*)\.mouseReleased\(([^,]+),\s*([^,]+),\s*button\)""").replace(current) {
        "com.pathmind.util.InputCompatibilityBridge.mouseReleased(${it.groupValues[1]}, ${it.groupValues[2]}, ${it.groupValues[3]}, button)"
    }
    current = Regex("""(?<![A-Za-z0-9_])(?!(?:super)\b)([A-Za-z_][A-Za-z0-9_.]*)\.mouseDragged\(([^,]+),\s*([^,]+),\s*button,\s*deltaX,\s*deltaY\)""").replace(current) {
        "com.pathmind.util.InputCompatibilityBridge.mouseDragged(${it.groupValues[1]}, ${it.groupValues[2]}, ${it.groupValues[3]}, button, deltaX, deltaY)"
    }
    current = Regex("""(?<![A-Za-z0-9_])(?!(?:super)\b)([A-Za-z_][A-Za-z0-9_.]*)\.keyPressed\(keyCode,\s*scanCode,\s*modifiers\)""").replace(current) {
        "com.pathmind.util.InputCompatibilityBridge.keyPressed(${it.groupValues[1]}, keyCode, scanCode, modifiers)"
    }
    current = Regex("""(?<![A-Za-z0-9_])(?!(?:super)\b)([A-Za-z_][A-Za-z0-9_.]*)\.charTyped\(chr,\s*modifiers\)""").replace(current) {
        "com.pathmind.util.InputCompatibilityBridge.charTyped(${it.groupValues[1]}, chr, modifiers)"
    }
    return current
}
val renamedMojangSourceDir = layout.buildDirectory.dir("generated/sources/renamedMojang/java")
val prepareRenamedMojangSources = tasks.register<Sync>("prepareRenamedMojangSources") {
    inputs.property("pathmindSourceTransform", "fabric-1.21.9-ui-input-v3-$usesRenamedMojangApis")
    from(rootProject.file("src/main/java")) {
        exclude("com/pathmind/PathmindMod.java")
        exclude("com/pathmind/PathmindClientMod.java")
        exclude("com/pathmind/util/LoaderInfo.java")
        exclude("com/pathmind/util/UseItemCallbackCompat.java")
        exclude("com/pathmind/mixin/GameRendererMixin.java")
    }
    from(rootProject.file("src/compat/legacy/base/java")) {
        exclude("com/pathmind/screen/PathmindMainMenuIntegration.java")
    }
    from(rootProject.file("src/compat/legacy/render/old/java"))
    from(rootProject.file("src/fabric/java/com/pathmind"))
    include("**/*.java")
    into(renamedMojangSourceDir)
    filteringCharset = "UTF-8"
    filter { line: String -> transformRenamedMojangLine(line) }
}

version = "$modVersion+mc$minecraftVersion-fabric"
group = providers.gradleProperty("maven_group").get()

base {
    archivesName.set("${providers.gradleProperty("archives_base_name").get()}-fabric")
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/pathmind.accesswidener")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("dev.architectury:architectury-fabric:$architecturyApiVersion")

    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets {
    main {
        java {
            if (usesGeneratedCompatibilitySources) {
                setSrcDirs(listOf(renamedMojangSourceDir))
            } else {
                setSrcDirs(
                    listOf(
                        rootProject.file("src/main/java"),
                        rootProject.file("src/compat/legacy/base/java"),
                        rootProject.file("src/compat/legacy/render/old/java"),
                        rootProject.file("src/fabric/java/com/pathmind")
                    )
                )
                exclude("com/pathmind/PathmindMod.java")
                exclude("com/pathmind/PathmindClientMod.java")
                exclude("com/pathmind/util/LoaderInfo.java")
                exclude("com/pathmind/util/UseItemCallbackCompat.java")
                exclude("com/pathmind/mixin/GameRendererMixin.java")
                exclude("com/pathmind/screen/PathmindMainMenuIntegration.java")
            }
        }
        resources {
            setSrcDirs(listOf(rootProject.file("src/main/resources")))
            exclude("META-INF/neoforge.mods.toml")
        }
    }
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraftVersion,
        "fabric_loader_version" to fabricLoaderVersion,
        "fabric_api_version" to fabricApiVersion,
        "architectury_api_version" to architecturyApiVersion,
    )
    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    if (usesGeneratedCompatibilitySources) {
        dependsOn(prepareRenamedMojangSources)
    }
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
