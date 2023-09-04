/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

plugins {
    id("com.android.library")
}

android {
    compileSdk = 31
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.netease.yunxin.kit:alog:1.0.9")
    implementation("com.netease.yunxin:nertc-base:5.4.8")
    implementation("com.netease.yunxin.kit.common:common:1.3.0")
    implementation("com.netease.yunxin.kit.common:common-ui:1.3.0")
    implementation(project(":entertainment:entertainment-common"))
    api("com.faceunity:core:8.6.0")
    api("com.faceunity:model:8.6.0")
}
