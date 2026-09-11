plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

import java.util.Properties

val localProperties = Properties().apply {
    rootDir.resolve("local.properties").inputStream().use(::load)
}

android {
    namespace = "com.tao.knowlegegraphtoefl"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.tao.knowlegegraphtoefl"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Default to the real cloud Firebase project so debug data survives rebuilds and
            // emulator restarts. Set firebaseUseEmulator=true in local.properties to opt back
            // into the local emulator suite for offline development.
            val useEmulator = localProperties.getProperty("firebaseUseEmulator", "false").toBoolean()
            val emulatorHost = localProperties.getProperty("firebaseEmulatorHost", "10.0.2.2")
            val emulatorProjectId = providers.gradleProperty("firebaseEmulatorProjectId")
                .orElse("knowlegegraphtoefl").get()
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"$emulatorHost\"")
            buildConfigField("String", "FIREBASE_EMULATOR_PROJECT_ID", "\"$emulatorProjectId\"")
            buildConfigField("Boolean", "USE_FIREBASE_EMULATOR", "$useEmulator")
        }
        release {
            optimization {
                enable = false
            }
            buildConfigField("String", "FIREBASE_EMULATOR_HOST", "\"\"")
            buildConfigField("String", "FIREBASE_EMULATOR_PROJECT_ID", "\"\"")
            buildConfigField("Boolean", "USE_FIREBASE_EMULATOR", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val androidSdk = providers.environmentVariable("ANDROID_HOME").orNull
    ?: localProperties.getProperty("sdk.dir")
val adbPath = androidSdk?.let { rootDir.resolve(it).resolve("platform-tools/adb").absolutePath } ?: "adb"

val reverseFirebaseFunctionsPort = tasks.register<Exec>("reverseFirebaseFunctionsPort") {
    executable(adbPath)
    args("reverse", "tcp:5001", "tcp:5001")
}
val reverseFirebaseAuthPort = tasks.register<Exec>("reverseFirebaseAuthPort") {
    executable(adbPath)
    args("reverse", "tcp:9099", "tcp:9099")
}
reverseFirebaseAuthPort.configure {
    mustRunAfter(reverseFirebaseFunctionsPort)
}
tasks.register("reverseFirebaseEmulatorPorts") {
    dependsOn(reverseFirebaseFunctionsPort, reverseFirebaseAuthPort)
}

tasks.matching { it.name == "installDebug" }.configureEach {
    dependsOn("reverseFirebaseEmulatorPorts")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}