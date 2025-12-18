plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

//    implementation(libs.spring.boot.starter.data.jpa)
//    implementation(libs.spring.boot.starter.flyway)

//    implementation(libs.flyway.core)
//    runtimeOnly(libs.flyway.database.postgresql)
//    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.testcontainers.postgresql)
}
