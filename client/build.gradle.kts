import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.text.SimpleDateFormat
import java.util.*

val conveyorCommand = "/Applications/Conveyor.app/Contents/MacOS/conveyor"
val conveyorInputDir = "${projectDir}/output"
// win, mac, mac-aarch64
val platform = if ("${System.getenv()["PLATFORM"]}" != "null") "${System.getenv()["PLATFORM"]}" else "mac-aarch64"
val buildDate:String = SimpleDateFormat("yyyyMMddHHmm").format(Date())

plugins {
    application
    kotlin("jvm") version "2.0.21"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass = "open.dolphin.client.Dolphin"
    applicationDefaultJvmArgs = listOf(
        "--add-opens=java.desktop/javax.swing.undo=ALL-UNNAMED",
    )
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.graphics", "javafx.swing")
    setPlatform(this@Build_gradle.platform.removeSurrounding("\""))
}

tasks {
    named<ShadowJar>("shadowJar") {
        // quisque "META-INF/services/jakarta.ws.rs.ext.Providers" should be merged
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
    }
    val deleteConveyorOutput = register<Delete>("delete-conveyor-output") {
        delete(conveyorInputDir)
    }
    val conveyor = register<Exec>("conveyor") {
        group = "distribution"
        description = "make app"
        dependsOn.add(shadowJar)
        dependsOn.add(deleteConveyorOutput)
        workingDir(projectDir)
        val jarName = shadowJar.get().archiveFileName.get()
        val projectVersion = project.property("version")
        val javaVersion = project.property("java.version")
        // win.amd64:windows-msix, mac.amd64:mac-app, mac.aarch64:mac-app
        val target = when(platform.removeSurrounding("\"")) {
            "win" -> arrayOf("win.amd64", "windows-msix")
            "mac" -> arrayOf("mac.amd64", "mac-app")
            else -> arrayOf("mac.aarch64", "mac-app")
        }
        commandLine = (listOf(
                conveyorCommand,
                "-Kjar.name=${jarName}",
                "-Kproject.version=${projectVersion}",
                "-Kbuild.date=${buildDate}",
                "-Kjava.version=${javaVersion}",
                "-Kapp.machines=${target[0]}",
                "make", target[1]))

    }
    register<Exec>("tar") {
        group = "distribution"
        description = "make tar"
        dependsOn.add(conveyor)
        commandLine = (listOf(
                "tar", "czf", "${conveyorInputDir}/OpenDolphin-${buildDate}.tgz",
                "-C", conveyorInputDir, "OpenDolphin.app"))
    }
    build {
        dependsOn.add(":server:jar")
    }
    clean {
        dependsOn(deleteConveyorOutput)
    }
}

repositories {
    mavenCentral()
    flatDir { dirs("../server/build/libs") }
}

dependencies {
    api(project(":server"))
    implementation(libs.jakarta)
    implementation(libs.resteasy.core)
    implementation(libs.resteasy.jackson2)
    implementation(libs.resteasy.client)
    implementation(libs.hibernate.core)
    implementation(libs.tyrus)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.google)
    implementation(libs.logback)
    implementation(libs.j2html)
    implementation(libs.angus.mail)
    implementation(libs.intellij.annotations)
    implementation(libs.flatlaf)
    implementation(libs.jdom2)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
}
