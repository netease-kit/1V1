// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.config;

import android.content.Context;
import android.graphics.Color;
import com.netease.nimlib.sdk.NotificationFoldStyle;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.ServerAddresses;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.StatusBarNotificationFilter;
import com.netease.nimlib.sdk.mixpush.MixPushConfig;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.activity.HomeActivity;
import com.netease.yunxin.app.oneonone.push.PushUserInfoProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.entertainment.common.AppStatusManager;

/** Nim SDK config info */
public class NimSDKOptionConfig {

  public static final String NOTIFY_SOUND_KEY =
      "android.resource://com.netease.yunxin.app.im/raw/msg";
  public static final int LED_ON_MS = 1000;
  public static final int LED_OFF_MS = 1500;

  public static SDKOptions getSDKOptions(Context context, String appKey) {
    SDKOptions options = new SDKOptions();
    options.appKey = appKey;
    initStatusBarNotificationConfig(options);
    options.preloadAttach = true;
    options.thumbnailSize = (int) (222.0 / 375.0 * ScreenUtils.getDisplayWidth());
    options.userInfoProvider = new PushUserInfoProvider(context);
    options.sessionReadAck = true;
    options.animatedImageThumbnailEnabled = true;
    options.asyncInitSDK = true;
    options.reducedIM = false;
    options.checkManifestConfig = false;
    options.enableTeamMsgAck = true;
    options.enableFcs = false;
    options.shouldConsiderRevokedMessageUnreadCount = true;
    // 会话置顶是否漫游
    options.notifyStickTopSession = true;
    //    options.mixPushConfig = buildMixPushConfig();
    options.serverConfig = configServer(context);
    // 打开消息撤回未读数-1的开关
    options.shouldConsiderRevokedMessageUnreadCount = true;
    return options;
  }

  public static ServerAddresses configServer(Context context) {

    if (AppConfig.isOversea()) {
      ServerAddresses serverAddresses = new ServerAddresses();
      serverAddresses.lbs = "https://lbs.netease.im/lbs/conf.jsp";
      serverAddresses.nosUploadLbs = "http://wannos.127.net/lbs";
      serverAddresses.nosUploadDefaultLink = "https://nosup-hz1.127.net";
      serverAddresses.nosDownloadUrlFormat = "{bucket}-nosdn.netease.im/{object}";
      serverAddresses.nosUpload = "nosup-hz1.127.net";
      serverAddresses.nosSupportHttps = true;
      ALog.d("ServerAddresses", "ServerConfig:use Singapore node");
      return serverAddresses;
    }
    return null;
  }

  public static void initStatusBarNotificationConfig(SDKOptions options) {
    // load notification
    StatusBarNotificationConfig config = loadStatusBarNotificationConfig();
    // load 用户的 StatusBarNotificationConfig 设置项
    // SDK statusBarNotificationConfig 生效
    config.notificationFilter =
        imMessage ->
            AppStatusManager.getInstance().getActiveCount() > 0
                ? StatusBarNotificationFilter.FilterPolicy.DENY
                : StatusBarNotificationFilter.FilterPolicy.DEFAULT;
    options.statusBarNotificationConfig = config;
  }

  // config StatusBarNotificationConfig
  public static StatusBarNotificationConfig loadStatusBarNotificationConfig() {
    StatusBarNotificationConfig config = new StatusBarNotificationConfig();
    config.notificationEntrance = HomeActivity.class;
    config.notificationSmallIconId = R.mipmap.ic_launcher;
    config.notificationColor = Color.parseColor("#3a9efb");
    config.notificationSound = NOTIFY_SOUND_KEY;
    config.notificationFoldStyle = NotificationFoldStyle.ALL;
    config.downTimeEnableNotification = true;
    config.ledARGB = Color.GREEN;
    config.ledOnMs = LED_ON_MS;
    config.ledOffMs = LED_OFF_MS;
    config.showBadge = true;
    return config;
  }

  private static MixPushConfig buildMixPushConfig() {
    MixPushConfig config = new MixPushConfig();
    // xiaomi
    config.xmAppId = "2882303761520055541";
    config.xmAppKey = "5222005592541";
    config.xmCertificateName = "KIT_MI_PUSH";

    // huawei
    config.hwAppId = "104776841";
    config.hwCertificateName = "KIT_HW_PUSH";

    // meizu
    config.mzAppId = "148192";
    config.mzAppKey = "6068c5a8323542deaf83ad5b6d3ca9e2";
    config.mzCertificateName = "KIT_MEIZU_PUSH";

    // fcm
    //        config.fcmCertificateName = "DEMO_FCM_PUSH";

    // vivo
    config.vivoCertificateName = "KIT_VIVO_PUSH";

    // oppo
    config.oppoAppId = "30795055";
    config.oppoAppKey = "6ffe2c1198c5448e84b75f3b78b711ce";
    config.oppoAppSercet = "f55d519d05a04360a8ba3404a24594a0";
    config.oppoCertificateName = "KIT_OPPO_PUSH";
    return config;
  }
}
