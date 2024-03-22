// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.utils.callkit.CustomCallOrderProvider;
import com.netease.yunxin.app.oneonone.ui.utils.callkit.PartyNERtcCallExtension;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.internal.NECallEngineImpl;
import com.netease.yunxin.kit.call.p2p.model.NECallInitRtcMode;
import com.netease.yunxin.kit.call.p2p.model.NEInviteInfo;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import com.netease.yunxin.nertc.ui.service.IncomingCallEx;
import org.json.JSONException;
import org.json.JSONObject;

public class CallKitUtil {
  private static final String TAG = "CallKitUtil";

  public static void initCallKit(
      long customRtcUid, String logDir, String appKey, IncomingCallEx incomingCallEx) {
    NERtcOption neRtcOption = new NERtcOption();
    neRtcOption.logLevel = NERtcConstants.LogLevel.INFO;
    if (!TextUtils.isEmpty(logDir)) {
      neRtcOption.logDir = logDir;
    }
    CallKitUIOptions.Builder builder =
        new CallKitUIOptions.Builder()
            // 必要：音视频通话 sdk appKey，用于通话中使用
            .rtcAppKey(appKey)
            // 非必要：这里是设置自定义Rtc uid，这里主要用于本Demo的业务服务器处理安全通业务逻辑，在音视频违规时，
            // 必要：当前用户 AccId
            .currentUserAccId(UserInfoManager.getSelfUserUuid())
            .currentUserRtcUId(customRtcUid)
            .timeOutMillisecond(CallConfig.CALL_TOTAL_WAIT_TIMEOUT)
            // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
            .notificationConfigFetcher(
                invitedInfo -> {
                  ALog.i(TAG, "invitedInfo:" + invitedInfo.toString());
                  return generateNotificationConfig(invitedInfo);
                })
            // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
            .resumeBGInvitation(true)
            .joinRtcWhenCall(true)
            .rtcCallExtension(new PartyNERtcCallExtension())
            .rtcSdkOption(neRtcOption)
            .initRtcMode(NECallInitRtcMode.IN_NEED_DELAY_TO_ACCEPT)
            .p2pAudioActivity(CallActivity.class)
            .p2pVideoActivity(CallActivity.class);
    if (incomingCallEx != null) {
      builder.incomingCallEx(incomingCallEx);
    }
    CallKitUIOptions options = builder.build();
    NECallEngine.sharedInstance().setCallRecordProvider(new CustomCallOrderProvider());
    CallKitUI.init(AppGlobals.getApplication(), options);
  }

  private static CallKitNotificationConfig generateNotificationConfig(NEInviteInfo invitedInfo) {
    CallKitNotificationConfig callKitNotificationConfig;
    String nickname = UserInfoManager.getSelfUserUuid();
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(invitedInfo.extraInfo);
      nickname = jsonObject.optString(AppParams.CALLER_USER_NAME);
    } catch (JSONException e) {
      e.printStackTrace();
      ALog.e(TAG, "e:" + e);
    }
    if (invitedInfo.callType == ChannelType.AUDIO.getValue()) {
      callKitNotificationConfig =
          new CallKitNotificationConfig(
              R.mipmap.ic_launcher,
              null,
              getAppName(XKitUtils.getApplicationContext()),
              nickname
                  + XKitUtils.getApplicationContext()
                      .getString(R.string.one_on_one_notification_new_incoming_audio_call));
    } else {
      callKitNotificationConfig =
          new CallKitNotificationConfig(
              R.mipmap.ic_launcher,
              null,
              getAppName(XKitUtils.getApplicationContext()),
              nickname
                  + XKitUtils.getApplicationContext()
                      .getString(R.string.one_on_one_notification_new_incoming_video_call));
    }

    return callKitNotificationConfig;
  }

  /** 获取应用程序名称 */
  public static synchronized String getAppName(Context context) {
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
      int labelRes = packageInfo.applicationInfo.labelRes;
      return context.getResources().getString(labelRes);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /** @return 是否在通话中 */
  public static boolean isInTheCall() {
    NECallEngineImpl callEngine = (NECallEngineImpl) NECallEngine.sharedInstance();
    CallState currentState = callEngine.getRecorder().getCurrentState();
    return currentState.getStatus() != CallState.STATE_IDLE;
  }
}
