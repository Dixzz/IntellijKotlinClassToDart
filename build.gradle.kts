plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("org.jetbrains.intellij") version "1.9.0"

    id ("org.openjfx.javafxplugin") version "0.0.13"
    id ("org.beryx.jlink") version "2.25.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://www.jitpack.io")
    jcenter()

    flatDir {
        dirs("lib")
    }
}

javafx {
    version = "17.0.2"
    modules = (listOf("javafx.controls", "javafx.fxml"))
}
dependencies {
    implementation(":rsyntaxtextarea-2.6.0")
    implementation("com.github.kotlinx.ast:grammar-kotlin-parser-antlr-kotlin:0.1.0")

    implementation("org.controlsfx:controlsfx:11.1.2")
    implementation("com.dlsc.formsfx:formsfx-core:11.5.0") {
        exclude("org.openjfx")
    }
    implementation("net.synedra:validatorfx:0.3.1") {
        exclude("org.openjfx")
    }
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3.3")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("android"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
