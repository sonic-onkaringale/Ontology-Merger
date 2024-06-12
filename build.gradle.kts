plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "2.0.0"
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