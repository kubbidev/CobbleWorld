import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    id("java-library")
    alias(libs.plugins.loom)
    alias(libs.plugins.shadow)
    id("maven-publish")
}

group = "me.kubbidev"
version = "1.0-SNAPSHOT"

base {
    archivesName.set("cobbleworld")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

    val apiModules = listOf(
        "fabric-api"
    )

    apiModules.forEach {
        modImplementation(fabricApi.module(it, "0.92.3+1.20.1"))
    }

    // test
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
}

fun determinePatchVersion(): Int {
    // get the name of the last tag
    val tagInfo = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = tagInfo
    }
    val tagString = String(tagInfo.toByteArray())
    if (tagString.contains("-")) {
        return tagString.split("-")[1].toInt()
    }
    return 0
}

val majorVersion = "1"
val minorVersion = "0"
val patchVersion = determinePatchVersion()
val releaseVersion = "$majorVersion.$minorVersion"
val projectVersion = "$releaseVersion.$patchVersion"

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "cobbleworld"
            version = releaseVersion

            from(components["java"])
            pom {
                name = "CobbleWorld"
                description = "A Minecraft mod used for the private Minecraft server of the same name."
                url = "https://github.com/kubbidev/CobbleWorld"

                licenses {
                    license {
                        name = "CC BY-NC-SA 4.0"
                        url = "https://creativecommons.org/licenses/by-nc-sa/4.0/"
                    }
                }

                developers {
                    developer {
                        id = "kubbidev"
                        name = "kubbi"
                        url = "https://kubbidev.me"
                    }
                }

                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/kubbidev/CobbleWorld/issues"
                }
            }
        }
    }
    repositories {
        maven(url = "https://nexus.kubbidev.me/repository/maven-releases/") {
            name = "kubbidev-releases"
            credentials(PasswordCredentials::class) {
                username = System.getenv("GRADLE_KUBBIDEV_RELEASES_USER")
                    ?: property("kubbidev-releases-user") as String?

                password = System.getenv("GRADLE_KUBBIDEV_RELEASES_PASS")
                    ?: property("kubbidev-releases-pass") as String?
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", projectVersion)
    filesMatching("**/fabric.mod.json") {
        expand("version" to projectVersion)
    }
}

tasks.shadowJar {
    archiveFileName = "cobbleworld-${projectVersion}-dev.jar"

    mergeServiceFiles()
    dependencies {
        exclude(dependency("net.fabricmc:.*"))
        include(dependency("me.kubbidev:.*"))

        // we don't want to include the mappings in the jar do we?
        exclude("/mappings/*")
    }
}

val remappedShadowJar by tasks.registering(RemapJarTask::class) {
    dependsOn(tasks.shadowJar)

    inputFile = tasks.shadowJar.flatMap {
        it.archiveFile
    }
    addNestedDependencies = true
    archiveFileName = "CobbleWorld-Fabric-${projectVersion}.jar"
}

tasks.assemble {
    dependsOn(remappedShadowJar)
}

tasks.publish {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test>().configureEach {
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
}

artifacts {
    archives(remappedShadowJar)
    archives(tasks.shadowJar)
}