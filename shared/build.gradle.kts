import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.1.0"
    id("com.google.devtools.ksp")
}

version = project.findProperty("projectVersion") as String

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release", "debug")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Supabase & Ktor
                implementation(project.dependencies.platform("io.github.jan-tennert.supabase:bom:3.2.6"))
                implementation("io.github.jan-tennert.supabase:postgrest-kt")
                implementation("io.github.jan-tennert.supabase:auth-kt")
                implementation("io.github.jan-tennert.supabase:realtime-kt")
                implementation("io.github.jan-tennert.supabase:storage-kt")
                implementation("io.ktor:ktor-client-core:3.2.2")
                implementation("io.ktor:ktor-client-content-negotiation:3.2.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.2")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

                // Logging
                implementation("io.github.aakira:napier:2.7.1")

                // DI
                implementation("io.insert-koin:koin-core:4.0.0")

                // Settings
                implementation("com.russhwolf:multiplatform-settings-no-arg:1.2.0")

                // Room
                // implementation("androidx.room:room-runtime:2.7.0-alpha01")
                // implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha01")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:3.2.2")
                implementation("org.whispersystems:signal-protocol-android:2.8.1")
                implementation("androidx.security:security-crypto:1.0.0")
            }
        }

        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.2.2")
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.2.2")
            }
        }
    }
}

dependencies {
    /*
    add("kspCommonMainMetadata", "androidx.room:room-compiler:2.7.0-alpha01")
    add("kspAndroid", "androidx.room:room-compiler:2.7.0-alpha01")
    add("kspIosX64", "androidx.room:room-compiler:2.7.0-alpha01")
    add("kspIosArm64", "androidx.room:room-compiler:2.7.0-alpha01")
    add("kspIosSimulatorArm64", "androidx.room:room-compiler:2.7.0-alpha01")
    */
}

android {
    namespace = "com.synapse.social.studioasinc.shared"
    compileSdk = 36
    buildToolsVersion = "36.0.0"
    defaultConfig {
        minSdk = 26

        buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL") ?: project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${System.getenv("SUPABASE_ANON_KEY") ?: project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
        buildConfigField("String", "SUPABASE_SYNAPSE_S3_ENDPOINT_URL", "\"${System.getenv("SUPABASE_SYNAPSE_S3_ENDPOINT_URL") ?: project.findProperty("SUPABASE_SYNAPSE_S3_ENDPOINT_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_SYNAPSE_S3_ENDPOINT_REGION", "\"${System.getenv("SUPABASE_SYNAPSE_S3_ENDPOINT_REGION") ?: project.findProperty("SUPABASE_SYNAPSE_S3_ENDPOINT_REGION") ?: ""}\"")
        buildConfigField("String", "SUPABASE_SYNAPSE_S3_ACCESS_KEY_ID", "\"${System.getenv("SUPABASE_SYNAPSE_S3_ACCESS_KEY_ID") ?: project.findProperty("SUPABASE_SYNAPSE_S3_ACCESS_KEY_ID") ?: ""}\"")
        buildConfigField("String", "SUPABASE_SYNAPSE_S3_ACCESS_KEY", "\"${System.getenv("SUPABASE_SYNAPSE_S3_ACCESS_KEY") ?: project.findProperty("SUPABASE_SYNAPSE_S3_ACCESS_KEY") ?: ""}\"")
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
