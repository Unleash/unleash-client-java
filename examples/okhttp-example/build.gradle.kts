plugins {
    java
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.getunleash:unleash-client-java:6.0.0-SNAPSHOT")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
}
