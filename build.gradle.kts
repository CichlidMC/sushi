plugins {
    id("java-library")
    id("maven-publish")
}

group = "io.github.cichlidmc"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/snapshots/")
//    maven("https://maven.neoforged.net/")
}

dependencies {
    api("io.github.cichlidmc:TinyCodecs:2.0.0")
    api("org.ow2.asm:asm-tree:9.7")
    compileOnlyApi("org.jetbrains:annotations:24.1.0")

//    api("com.jetbrains.intellij.java:java-psi-api:233.11799.300")
//    implementation("net.neoforged.jst:jst-cli:1.0.69")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven("https://mvn.devos.one/snapshots") {
            name = "devOS"
            credentials(PasswordCredentials::class)
        }
    }
}
