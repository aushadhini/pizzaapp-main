plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pizzaapp"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.pizzaapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // AndroidX UI
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // SplashScreen API
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.material:material:1.12.0")

        implementation("com.google.android.gms:play-services-location:21.3.0")
        // Firestore is already in your project; if not:
        // implementation("com.google.firebase:firebase-firestore:25.1.1")

// app/build.gradle.kts
    dependencies {
        implementation(platform("com.google.firebase:firebase-bom:33.2.0")) // or newer
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        // If you directly use Play Services APIs, ensure they’re recent:
        implementation("com.google.android.gms:play-services-base:18.3.0")
    }



    // Firebase (ok to keep even if we don’t use it in baseline)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    dependencies {
        // Import the Firebase BoM
        implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

        // Firebase Auth
        implementation("com.google.firebase:firebase-auth")

        // Firestore (if needed)
        implementation("com.google.firebase:firebase-firestore")
    }


}
