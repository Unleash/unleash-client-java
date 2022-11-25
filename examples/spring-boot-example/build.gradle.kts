plugins {
  java
  id("org.springframework.boot") version "3.0.0"
  id("io.spring.dependency-management") version "1.1.0"
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("io.getunleash:unleash-client-java:7.0.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
