import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import java.nio.file.Paths

plugins {
    id("io.freefair.lombok") version "6.3.0"
    `cpp-library`
    `java-library`
    `maven-publish`
}

group = "me.kcra"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

library {
    val osProperty: String = System.getProperty("os.name", "unknown")?.toLowerCase()
        ?: throw RuntimeException("Unsupported OS")
    val osName: String = if (osProperty.contains("mac") || osProperty.contains("darwin")) {
        "mac_osx"
    } else if (osProperty.contains("win")) {
        "windows"
    } else if (osProperty.contains("nux")) {
        "linux"
    } else {
        throw IllegalArgumentException("Unsupported OS")
    }

    var archBits = 64
    val dataModel: String? = System.getProperty("sun.arch.data.model")
    if (dataModel != null && dataModel.contains("32")) {
        archBits = 32
    } else {
        val osArch: String? = System.getProperty("os.arch")
        if (osArch != null && (osArch.contains("86") && !osArch.contains("64") || osArch.contains("32"))) {
            archBits = 32
        }
    }
    baseName.set("reflect-$osName-$archBits")
}

tasks.register("buildNative", Copy::class.java) {
    dependsOn("compileJava", "linkRelease")

    from(Paths.get(buildDir.absolutePath, "lib", "main", "release"))
    into(sourceSets["main"].resources.srcDirs.first())
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-h", file("src/main/cpp").absolutePath))
}

tasks.withType<Jar> {
    val list: Array<String>? = sourceSets["main"].resources.srcDirs.first().list()
    if (list == null || list.isEmpty()) {
        dependsOn("buildNative")
    }
}

tasks.withType<CppCompile> {
    compilerArgs.add("-I${Jvm.current().javaHome}/include")

    when {
        OperatingSystem.current().isWindows -> compilerArgs.add("-I${Jvm.current().javaHome}/include/win32")
        OperatingSystem.current().isLinux -> compilerArgs.add("-I${Jvm.current().javaHome}/include/linux")
        OperatingSystem.current().isMacOsX -> compilerArgs.add("-I${Jvm.current().javaHome}/include/darwin")
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("reflect")
                description.set("A Java library for manipulation with JVM internals")
                url.set("https://github.com/zlataovce/reflect")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/zlataovce/reflect/blob/master/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("zlataovce")
                        name.set("Matouš Kučera")
                        email.set("mk@kcra.me")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/zlataovce/reflect.git")
                    developerConnection.set("scm:git:ssh://github.com/zlataovce/reflect.git")
                    url.set("https://github.com/zlataovce/reflect/tree/master")
                }
            }
        }
    }

    repositories {
        maven {
            url = if ((project.version as String).endsWith("-SNAPSHOT")) uri("https://repo.kcra.me/snapshots")
                else uri("https://repo.kcra.me/releases")
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
}