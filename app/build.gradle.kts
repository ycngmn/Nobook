import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

extensions.configure<ApplicationExtension> {
    namespace = "com.ycngmn.nobook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ycngmn.nobook"
        minSdk = 23
        targetSdk = 36
        versionCode = 11
        versionName = "0.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { applicationIdSuffix = ".test" }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.compose.webview.multiplatform)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.metrics.performance)
    testImplementation(libs.playwright)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// ─── TS bundling ────────────────────────────────────────────────────
// Transpiles app/src/main/ts/*.ts -> app/src/main/res/raw/*.js (Phase 2.2).
// Three chained Exec tasks (install / type-check / bundle); tsBundle runs
// on every preBuild so res/raw stays in sync with ts sources. The chain is
// a no-op until Phase 2.6 ports the first .ts file (esbuild.config.js prints
// "No .ts source files to transpile." and exits 0 today).
tasks.register<Exec>("tsInstall") {
    group = "build"
    description = "pnpm install app/src/main/ts deps (idempotent)"
    workingDir = file("src/main/ts")
    commandLine("pnpm", "install", "--no-frozen-lockfile", "--silent")
}

tasks.register<Exec>("tsTypeCheck") {
    group = "verification"
    description = "tsc --noEmit for app/src/main/ts"
    workingDir = file("src/main/ts")
    commandLine("node", "node_modules/typescript/bin/tsc", "--noEmit")
    dependsOn("tsInstall")
}

tasks.register<Exec>("tsBundle") {
    group = "build"
    description = "Emit app/src/main/res/raw/*.js from app/src/main/ts/*.ts via esbuild"
    workingDir = file("src/main/ts")
    commandLine("node", "esbuild.config.js")
    dependsOn("tsTypeCheck")
}

tasks.named("preBuild") {
    dependsOn("tsBundle")
}