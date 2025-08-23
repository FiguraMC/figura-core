plugins {
    id("java-library")
    id("maven-publish")
    id("cobalt-instrumentation") version "1.0-SNAPSHOT"
}

tasks.cobaltInstrumentation { dependsOn(*tasks.withType<JavaCompile>().toTypedArray()) }
tasks.jar { dependsOn(tasks.cobaltInstrumentation) }

group = "org.figuramc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Other Figura repos which this uses
    implementation("org.figuramc:memory-tracker:1.0-SNAPSHOT")
    implementation("org.figuramc:figura-translations:1.0-SNAPSHOT")
    implementation("org.figuramc:figura-molang:1.0-SNAPSHOT")
    implementation("org.figuramc:figura-cobalt:1.0-SNAPSHOT")

    // Subproject annotation processor
    compileOnly(project(":figura-comptime"))
    annotationProcessor(project(":figura-comptime"))

    // Provided by Minecraft at runtime, I think?
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains:annotations:25.0.0")
    implementation("org.joml:joml:1.10.8")
}

java {
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}