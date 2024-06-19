import org.lwjgl.Lwjgl.Module.*
import org.lwjgl.Release
import org.lwjgl.lwjgl

plugins {
    kotlin("jvm") version "1.9.23"

    id("org.lwjgl.plugin") version "0.0.34"
}

group = "dev.runefox.matlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.shadew.net/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("net.shadew:geotest:0.1")
    lwjgl {
        version = Release.`3_3_1`
        testImplementation(opengl, glfw, nanovg, stb)
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
