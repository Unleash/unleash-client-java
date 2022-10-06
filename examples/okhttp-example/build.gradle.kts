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
    implementation("io.getunleash:unleash-client-java:6.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}
