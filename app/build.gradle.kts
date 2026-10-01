plugins { id("com.android.application") }

val playId = providers.gradleProperty("playApplicationId").orElse("org.sindhipinyin.keyboard.dev")
val uploadStore = providers.environmentVariable("SINDHI_UPLOAD_STORE_FILE").orNull

android {
    namespace = "org.sindhipinyin.keyboard"
    // Available official SDK repository currently supplies 36; target remains 37.
    // No API 37-only methods are used. Upgrade compileSdk when platform 37 is available.
    compileSdk = 36
    defaultConfig {
        applicationId = playId.get()
        minSdk = 24
        targetSdk = 37
        versionCode = providers.gradleProperty("playVersionCode").orElse("5").get().toInt()
        versionName = providers.gradleProperty("playVersionName").orElse("0.5.0-alpha").get()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    if (uploadStore != null) {
        signingConfigs.create("playUpload") {
            storeFile = file(uploadStore)
            storePassword = providers.environmentVariable("SINDHI_UPLOAD_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("SINDHI_UPLOAD_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("SINDHI_UPLOAD_KEY_PASSWORD").get()
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (uploadStore != null) signingConfig = signingConfigs.getByName("playUpload")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    if (rootProject.file(".tools/debug.keystore").exists()) signingConfigs.getByName("debug") {
        storeFile = rootProject.file(".tools/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }
    sourceSets.getByName("main").assets.directories.add("../engine/src/main/resources")
    lint { abortOnError = true }
}

dependencies {
    implementation(project(":engine"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
