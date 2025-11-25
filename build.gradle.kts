plugins {
    alias(libs.plugins.java.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.extra.java.module.info)
    jacoco
}

group = "fish.cichlidmc"
version = "0.2.0"

repositories {
    exclusiveContent {
        forRepositories(mavenCentral()).filter {
            includeModule("org.jetbrains", "annotations")
            includeModule("org.glavo", "classfile")
            includeModule("org.vineflower", "vineflower")
            includeModule("org.jacoco", "org.jacoco.agent")

            includeGroupAndSubgroups("org.junit")
            // all of these are used by Junit
            includeModule("org.apiguardian", "apiguardian-api")
            includeModule("org.jspecify", "jspecify")
            includeModule("org.opentest4j", "opentest4j")
        }
        forRepositories(maven("https://mvn.devos.one/releases/")).filter {
            includeModule("fish.cichlidmc", "tiny-json")
            includeModule("fish.cichlidmc", "tiny-codecs")
        }
    }
}

dependencies {
    compileOnlyApi(libs.jetbrains.annotations)
    api(libs.tiny.codecs)
    api(libs.classfile.api)

    testImplementation(libs.vineflower)
    testImplementation(libs.bundles.junit)
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

tasks.test {
    useJUnitPlatform()
    // enables discovering the Junit extension via ServiceLoader
    jvmArgs("-Djunit.jupiter.extensions.autodetection.enabled=true")
}

tasks.jacocoTestReport {
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
