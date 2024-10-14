plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // 注意Kotlin DSL中是 `url = uri()`
}

dependencies {
    implementation ("io.github.java-diff-utils:java-diff-utils:4.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.fifesoft:rsyntaxtextarea:3.5.1")

    // 手动指定兼容的 SLF4J 版本

    compileOnly("org.slf4j:slf4j-api:1.7.30")
}

configurations.all {
    resolutionStrategy {
        force("org.slf4j:slf4j-api:1.7.30") // 选择唯一版本
    }
}


//sourceSets {
//    main {
//        resources {
//            srcDir("src/main/resources")
//        }
//    }
//}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
