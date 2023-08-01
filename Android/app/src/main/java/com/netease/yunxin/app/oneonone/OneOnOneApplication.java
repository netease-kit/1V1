// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone;

import android.app.Application;
import com.faceunity.nama.FURenderer;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.ServerAddresses;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.config.NimSDKOptionConfig;
import com.netease.yunxin.app.oneonone.config.OverSeaConfig;
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
  // 如果返回值为 null，则全部使用默认参数。
  private SDKOptions options() {
    SDKOptions options = new SDKOptions();
    //此处仅设置appkey，其他设置请自行参看信令文档设置 ：https://dev.yunxin.163.com/docs/product/信令/SDK开发集成/Android开发集成/初始化
    options.appKey = AppConfig.getAppKey();
    if (AppConfig.IS_OVERSEA) {
      //海外环境
      ServerAddresses serverAddresses = new ServerAddresses();
      serverAddresses.defaultLink = OverSeaConfig.LINK;
      serverAddresses.lbs = OverSeaConfig.LBS;
      serverAddresses.nosUploadLbs = OverSeaConfig.NOS_LBS;
      serverAddresses.nosUploadDefaultLink = OverSeaConfig.NOS_UPLOADER;
      serverAddresses.nosDownloadUrlFormat = OverSeaConfig.NOS_DOWNLOADER;
      serverAddresses.nosUpload = OverSeaConfig.NOS_UPLOADER_HOST;
      serverAddresses.nosSupportHttps = true;
      options.serverConfig = serverAddresses;
    }
    return options;
  }
}
