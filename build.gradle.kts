plugins {
    id("java-library")
    id("maven-publish")
    id("org.gradlex.extra-java-module-info") version "1.13"
    jacoco
}

group = "fish.cichlidmc"
version = "0.2.0"

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/releases/")
}

dependencies {
    api("fish.cichlidmc:tiny-codecs:3.2.0")
    api("org.glavo:classfile:0.5.0")
    compileOnlyApi("org.jetbrains:annotations:24.1.0")

    testImplementation("org.vineflower:vineflower:1.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// need to convert legacy non-module dependencies to modules so gradle makes them available to compilation
extraJavaModuleInfo {
    module("org.vineflower:vineflower", "org.vineflower.vineflower")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.compileJava {
    options.javaModuleVersion = provider { version as String }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs("-Djunit.jupiter.extensions.autodetection.enabled=true")
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn("test")

    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        listOf("Releases", "Snapshots").forEach {
            maven("https://mvn.devos.one/${it.lowercase()}") {
                name = "devOs$it"
                credentials(PasswordCredentials::class)
            }
        }
    }
}
