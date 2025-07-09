plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'androidx.room'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.example.quadraticsolver"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
                targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
    buildFeatures {
        viewBinding true
    }
    room {
        schemaDirectory "$projectDir/schemas"
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'com.github.mikephil.charting:mpandroidchart:3.1.0'
    implementation 'com.google.mlkit:text-recognition:16.0.0'
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
    implementation 'org.json:json:20231013'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}