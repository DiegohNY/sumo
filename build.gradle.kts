plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.spotless)
    jacoco
}

group = project.property("group") as String
version = project.property("version") as String

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.spigot.api)

    // Shaded (server JVM does not provide these classes).
    implementation(libs.adventure.api)
    implementation(libs.adventure.minimessage)
    implementation(libs.adventure.legacy)
    implementation(libs.adventure.platform.bukkit)

    // Declared via plugin.yml `libraries:` so the server downloads them at runtime.
    // Listed compileOnly here so we can still compile against them locally.
    compileOnly(libs.hikari)
    compileOnly(libs.sqlite)
    compileOnly(libs.mariadb)

    testImplementation(libs.spigot.api)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.adventure.plain)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.hikari)
    testImplementation(libs.sqlite)
    testImplementation(libs.mariadb)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

tasks.shadowJar {
    archiveClassifier.set("")
    val shadeBase = "${project.group}.shadow"
    relocate("net.kyori", "$shadeBase.kyori")
}

tasks.build { dependsOn(tasks.shadowJar) }

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
    )
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.runServer {
    minecraftVersion("1.20.6")
}

spotless {
    java {
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
