plugins {
    id("java")
}

group = "com.preciouso"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/net.dv8tion/JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.13")
}

tasks.test {
    useJUnitPlatform()
}