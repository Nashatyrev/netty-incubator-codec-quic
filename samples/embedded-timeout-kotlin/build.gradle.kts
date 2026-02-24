plugins {
    kotlin("jvm") version "2.2.21"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenLocal()
    mavenCentral()
}

val quicVersion = "0.0.76.Final-SNAPSHOT"

fun nativeClassifier(): String {
    val os = System.getProperty("os.name").lowercase()
    val archRaw = System.getProperty("os.arch").lowercase()
    val arch = when (archRaw) {
        "aarch64", "arm64" -> "aarch_64"
        "x86_64", "amd64" -> "x86_64"
        else -> archRaw
    }
    val osPart = when {
        os.contains("mac") || os.contains("darwin") -> "osx"
        os.contains("linux") -> "linux"
        os.contains("win") -> "windows"
        else -> error("Unsupported OS for this sample: $os")
    }
    return "$osPart-$arch"
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.netty.incubator:netty-incubator-codec-classes-quic:$quicVersion")
    testImplementation("io.netty.incubator:netty-incubator-codec-native-quic:$quicVersion")
    testRuntimeOnly("io.netty.incubator:netty-incubator-codec-native-quic:$quicVersion:${nativeClassifier()}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}
