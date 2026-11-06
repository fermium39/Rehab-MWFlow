plugins { id("com.android.application") }
android {
    namespace = "com.fitflow.go"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.fitflow.go"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2026.11.06.01"
    }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies { implementation("androidx.webkit:webkit:1.12.1") }
