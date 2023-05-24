// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.service.XKitServiceManager;

public class OneOnOneUtils {
  public static final String TAG = "OneOnOneUtils";

  public static boolean isInVoiceRoom() {
    Object result =
        XKitServiceManager.Companion.getInstance()
            .callService("VoiceRoomKit", "getCurrentRoomInfo", null);
    if (result instanceof Boolean) {
      ALog.d(TAG, "isInVoiceRoom:" + result);
      return (boolean) result;
    }
    return false;
  }
}
