// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone;

import android.app.Application;
import com.faceunity.nama.FURenderer;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.config.NimSDKOptionConfig;
import com.netease.yunxin.app.oneonone.ui.utils.IMUIKitUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;
import com.netease.yunxin.kit.entertainment.common.utils.IconFontUtil;

public class OneOnOneApplication extends Application {

  private static final String TAG = "OneOnOneApplication";

  @Override
  public void onCreate() {
    super.onCreate();
    ALog.init(this, ALog.LEVEL_ALL);
    AppConfig.init(this);
    AppStatusManager.init(this);
    IconFontUtil.getInstance().init(this);
    FURenderer.getInstance().init(this);
    IMUIKitUtil.initIMUIKit(this, NimSDKOptionConfig.getSDKOptions(this, AppConfig.getAppKey()));
  }
}
