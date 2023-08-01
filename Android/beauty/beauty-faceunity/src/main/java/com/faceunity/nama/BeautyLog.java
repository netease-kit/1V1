// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama;

import android.os.Build;
import com.netease.yunxin.kit.alog.ALog;

public class BeautyLog {
  private static final String PREFIX = "[NEBeauty]->" + Build.BRAND + ":";

  public static void d(String tag, String msg) {
    ALog.d(PREFIX + tag, msg);
  }

  public static void e(String tag, String msg) {
    ALog.e(PREFIX + tag, msg);
  }

  public static void i(String tag, String msg) {
    ALog.i(PREFIX + tag, msg);
  }
}
