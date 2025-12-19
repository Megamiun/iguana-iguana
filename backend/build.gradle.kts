plugins {
    java
    alias(libs.plugins.lombok)

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

    implementation(libs.spring.boot.starter.data.jpa)

    implementation(libs.hibernate.dialects)
    implementation(libs.sqlite.jdbc)

    testImplementation(libs.spring.boot.starter.test)
}
