plugins {
    java
    application
}

application {
    mainClass.set("io.getunleash.example.UnleashOkHttp")
}
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.getunleash:unleash-client-java:8.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}
