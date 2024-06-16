plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "2.0.0"
    application
}

group = "org.onkaringale"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.github.amikos-tech:chromadb-java-client:0.1.5")

    implementation ("com.knuddels:jtokkit:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
//    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")


    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.apache.jena:apache-jena-libs:5.0.0")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("net.sourceforge.owlapi:owlapi-distribution:5.5.0")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("org.onkaringale.MainKt") // Replace with your main class
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.onkaringale.MainKt"
    }
    // To include dependencies in the JAR, uncomment the following lines:
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("src/main/resources") {
        include("**/*")
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_19) // Set the target JVM version
        }
    }
}