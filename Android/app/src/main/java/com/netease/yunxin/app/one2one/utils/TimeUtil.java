// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils;

import java.util.Locale;

public class TimeUtil {
  public static String formatSecondTime(long second) {
    if (second <= 0) {
      return "00:00";
    }
    int m = (int) (second / 60);
    int s = (int) (second % 60);
    int h = (int) (second / 3600);
    if (h <= 0) {
      return String.format(Locale.CHINA, "%02d:%02d", m, s);
    } else {
      return String.format(Locale.CHINA, "%02d:%02d:%02d", h, m, s);
    }
  }
}
