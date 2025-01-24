// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.constant.CallType;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.model.NECallPushConfig;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.entertainment.common.activity.WebViewActivity;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.CallParam;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class NavUtils {
  private static final String TAG = "NavUtils";

  public static void toBrowsePage(Context context, String title, String url) {
    Intent intent = new Intent(context, WebViewActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(AppParams.PARAM_KEY_TITLE, title);
    intent.putExtra(AppParams.PARAM_KEY_URL, url);

    context.startActivity(intent);
  }

  public static void toCallPage(Context context, UserModel userModel, int channelType) {
    JSONObject extraInfo = new JSONObject();
    try {
      extraInfo.putOpt(AppParams.CALLER_USER_NAME, UserInfoManager.getSelfNickname());
      extraInfo.putOpt(AppParams.CALLER_USER_AVATAR, UserInfoManager.getSelfImAvatar());
      if (userModel != null) {
        if (!TextUtils.isEmpty(userModel.nickname)) {
          extraInfo.putOpt(AppParams.CALLED_USER_NAME, userModel.nickname);
        }
        if (!TextUtils.isEmpty(userModel.mobile)) {
          extraInfo.putOpt(AppParams.CALLED_USER_MOBILE, userModel.mobile);
        }
        if (!TextUtils.isEmpty(userModel.avatar)) {
          extraInfo.putOpt(AppParams.CALLED_USER_AVATAR, userModel.avatar);
        }
        if (CallConfig.enableVirtualCall) {
          extraInfo.putOpt(
              AppParams.CALLED_IS_VIRTUAL, userModel.callType == CallType.VIRTUAL_CALL_TYPE);
          if (!TextUtils.isEmpty(userModel.audioUrl)) {
            extraInfo.putOpt(AppParams.CALLED_AUDIO_URL, userModel.audioUrl);
          }
          if (!TextUtils.isEmpty(userModel.videoUrl)) {
            extraInfo.putOpt(AppParams.CALLED_VIDEO_URL, userModel.videoUrl);
          }
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    // 呼叫组件的初始化依赖/nemo/socialChat/user/login 网络请求，可能存在呼叫比初始化快的时候，做下异常保护
    if (!CallKitUI.INSTANCE.getInit()) {
      ToastX.showShortToast("callkit has not init");
      return;
    }
    String extra = extraInfo.toString();
    CallParam param =
        new CallParam.Builder()
            .callType(channelType)
            .callExtraInfo(extra)
            .calledAccId(userModel.account)
            .pushConfig(providePushConfig(extra))
            .build();
    CallKitUI.startSingleCall(context, param);
  }

  public static NECallPushConfig providePushConfig(String extraInfo) {
    String nickname = UserInfoManager.getSelfUserUuid();
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(extraInfo);
      nickname = jsonObject.optString(AppParams.CALLER_USER_NAME);
    } catch (JSONException e) {
      ALog.e(TAG, "e:" + e);
    }
    HashMap<String, Object> pushPayload = new HashMap<>();
    pushPayload.put("attachment", extraInfo);
    return new NECallPushConfig(
        true, CallKitUtil.getAppName(XKitUtils.getApplicationContext()), nickname, pushPayload);
  }

  public static void toCallAudioPage(Context context, UserModel userModel) {
    toCallPage(context, userModel, ChannelType.AUDIO.getValue());
  }

  public static void toCallVideoPage(Context context, UserModel userModel) {
    toCallPage(context, userModel, ChannelType.VIDEO.getValue());
  }

  public static void toP2pPage(Context context, UserModel userInfo, String content) {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
        .withParam(RouterConstant.CHAT_KRY, userInfo)
        .withParam(RouterConstant.CHAT_ID_KRY, userInfo.account)
        .withParam("content", content)
        .withContext(context)
        .navigate();
  }
}
