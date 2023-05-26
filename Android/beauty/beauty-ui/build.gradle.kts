/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 31
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // androidx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.google.android.material:material:1.5.0")
    implementation("com.netease.yunxin:nertc-base:4.6.50")
    implementation("com.netease.yunxin.kit:alog:1.0.9")

    implementation("androidx.appcompat:appcompat:1.4.1")


    implementation("com.gyf.immersionbar:immersionbar:3.0.0")
    implementation("com.netease.yunxin.kit.common:common-ui:1.1.20")
    implementation("com.blankj:utilcodex:1.30.6")


}

