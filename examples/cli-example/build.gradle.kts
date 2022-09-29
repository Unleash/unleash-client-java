plugins {
    java
    application
}

application {
    mainClass.set("io.getunleash.example.AdvancedConstraints")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.getunleash:unleash-client-java:6.0.1")
}
