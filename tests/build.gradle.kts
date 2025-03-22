plugins {
    id("java")
    id("application")
}

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/snapshots/")
}

val input: Configuration by configurations.creating {
    isCanBeResolved = true
}

dependencies {
    implementation(project(":"))
    implementation("org.vineflower:vineflower:1.11.1")
    input(project(":test-app"))
}

java {
    toolchain {
        // for Vineflower
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "io.github.cichlidmc.sushi.test.runner.Main"
}

tasks.named("run") {
    group = "verification"
    dependsOn("setup")
}

tasks.register<Copy>("setup") {
    group = "verification"
    from(input)
    into(file("input"))
}
