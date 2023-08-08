
/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            setUrl("https://developer.huawei.com/repo/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            setUrl("https://maven.faceunity.com/repository/maven-public/")
        }
        jcenter()
    }
}

include(":app")
include(":oneonone:oneonone-ui")
include(":entertainment:entertainment-common")
include(":beauty:beauty-faceunity")
include(":imkit:chatkit-ui")
include(":imkit:conversationkit-ui")
include(":imkit:locationkit")

