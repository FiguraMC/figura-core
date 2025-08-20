plugins {
    id("java")
}

group = "org.figuramc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
}

tasks.test {
    useJUnitPlatform()
}