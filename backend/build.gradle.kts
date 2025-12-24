plugins {
    java
    alias(libs.plugins.lombok)

    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    id("org.openapi.generator") version "7.10.0"
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
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

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
