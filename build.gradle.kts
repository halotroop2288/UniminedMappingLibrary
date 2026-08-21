@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider()
    kotlin("plugin.serialization") version libs.versions.kotlin.asProvider()
    alias(libs.plugins.commons)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.resources)
    `maven-publish`
}

allprojects {
    pluginManager.apply("base")

    group = project.property("group").toString()
    base.archivesName = project.property("archives_base_name").toString()

    version = project.property("version") as String
    if (project.hasProperty("version_snapshot")) version = "$version-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://maven.wagyourtail.xyz/releases")
        maven("https://maven.wagyourtail.xyz/snapshots")
    }
}

kotlin {
    jvmToolchain(8)
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
    js {
        useCommonJs()
        browser {
        }
        nodejs()
        binaries.executable()
        }
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.commons.kt)
                api(libs.kotlin.logging)
                api(libs.okio)
                api(libs.jetbrains.annotations.kmp)
                api(libs.kotlin.coroutines)
                api(libs.kotlin.serialization.json)
                api(libs.kmp.zip)
                api(libs.kmp.zip.okio)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlin.coroutines.tests)
                implementation(libs.resources)
            }
        }
        val nonJvmMain = create("nonJvmMain") {
            dependsOn(commonMain.get())
        }
        jvmMain {
            dependencies {
                api(libs.bundles.asm)

                api(libs.slf4j.api)
                api(libs.slf4j.simple)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        webMain {
            dependsOn(nonJvmMain)
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        nativeMain {
            dependsOn(nonJvmMain)
        }
    }
}

tasks.getByName("allTests") {
    dependsOn(tasks.getByName("kotlinUpgradeYarnLock"))
}

publishing {
    repositories {
        maven {
            name = "WagYourMaven"
            url = if (project.hasProperty("version_snapshot")) {
                uri("https://maven.wagyourtail.xyz/snapshots/")
            } else {
                uri("https://maven.wagyourtail.xyz/releases/")
            }
            credentials {
                username = project.findProperty("mvn.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("mvn.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
