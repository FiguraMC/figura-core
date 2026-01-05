plugins {
    id("java-library")
}

group = "org.figuramc"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service:1.1.1")

    api("org.figuramc:figura-cobalt:1.0-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}