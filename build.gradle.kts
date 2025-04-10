plugins {
    id("java-library")
    id("maven-publish")
    jacoco
}

group = "io.github.cichlidmc"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/snapshots/")
//    maven("https://maven.neoforged.net/")
}

dependencies {
    api("io.github.cichlidmc:TinyCodecs:3.1.0")
    api("org.ow2.asm:asm-tree:9.7")
    compileOnlyApi("org.jetbrains:annotations:24.1.0")

//    api("com.jetbrains.intellij.java:java-psi-api:233.11799.300")
//    implementation("net.neoforged.jst:jst-cli:1.0.69")

    testImplementation("org.vineflower:vineflower:1.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    javaLauncher = javaToolchains.launcherFor {
        // need 17 for vineflower, might as well go all in
        languageVersion = JavaLanguageVersion.of(23)
    }
}

// compiler java version needs to match what's set above
tasks.named<JavaCompile>("compileTestJava") {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(23)
    }
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
        maven("https://mvn.devos.one/snapshots") {
            name = "devOS"
            credentials(PasswordCredentials::class)
        }
    }
}
