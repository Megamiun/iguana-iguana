plugins {
    java
    alias(libs.plugins.lombok)

    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    alias(libs.plugins.openapi.generator)
}

repositories {
    mavenCentral()
}

java {
    version = 21
}

dependencies {
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)

    implementation(libs.hibernate.dialects)
    implementation(libs.sqlite.jdbc)

    // OpenAPI dependencies
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.javax.annotation.api)
    implementation(libs.validation.api)

    testImplementation(libs.spring.boot.starter.test)
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generate-resources/main/src/main/java")
        }
    }
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${project.projectDir}/src/main/resources/openapi.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generate-resources/main")
    apiPackage.set("br.com.gabryel.maplewood.api")
    modelPackage.set("br.com.gabryel.maplewood.api.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useTags" to "true",
        "skipDefaultInterface" to "true",
        "documentationProvider" to "none"
    ))
    additionalProperties.set(mapOf(
        "openApiNullable" to "false"
    ))
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
