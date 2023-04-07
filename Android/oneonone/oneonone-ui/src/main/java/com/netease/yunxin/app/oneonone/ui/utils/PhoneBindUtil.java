// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.blankj.utilcode.util.SPUtils;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;

public class PhoneBindUtil {
  private static final String KEY = "lastPhoneNumber";

  public static void setLastBindPhoneNumber(String phoneNumber) {
    SPUtils.getInstance().put(KEY + OneOnOneUI.getInstance().getAppKey(), phoneNumber);
  }

  public static String getLastBindPhoneNumber() {
    return SPUtils.getInstance().getString(KEY + OneOnOneUI.getInstance().getAppKey(), "");
  }
}
