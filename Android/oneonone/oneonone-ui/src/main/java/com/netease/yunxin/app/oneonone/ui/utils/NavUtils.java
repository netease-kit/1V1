// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.activity.UserInfoActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.constant.CallType;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.entertainment.common.activity.WebViewActivity;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.CallParam;
import java.util.Map;
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

  public static void toCallPage(
      Context context,
      UserModel userModel,
      int channelType,
      boolean needPstnCall,
      long callPstnWaitMilliseconds) {
    JSONObject extraInfo = new JSONObject();
    try {
      extraInfo.putOpt(AppParams.CALLER_USER_NAME, UserInfoManager.getSelfNickname());
      extraInfo.putOpt(AppParams.CALLER_USER_MOBILE, UserInfoManager.getSelfPhoneNumber());
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
      extraInfo.putOpt(AppParams.NEED_PSTN_CALL, needPstnCall);
      extraInfo.putOpt(AppParams.CALL_PSTN_WAIT_MILLISECONDS, callPstnWaitMilliseconds);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    CallParam param =
        CallParam.createSingleCallParam(
            channelType, UserInfoManager.getSelfImAccid(), userModel.imAccid, extraInfo.toString());
    CallKitUI.startSingleCall(context, param);
  }

  public static void toCallAudioPage(Context context, UserModel userModel) {
    toCallPage(
        context,
        userModel,
        ChannelType.AUDIO.getValue(),
        AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid())
            && OneOnOneUI.getInstance().isChineseEnv(),
        CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
  }

  public static void toCallVideoPage(Context context, UserModel userModel) {
    toCallPage(
        context,
        userModel,
        ChannelType.VIDEO.getValue(),
        false,
        CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
  }

  public static void toCallAudioPage(
      Context context, UserInfo userInfo, long callPstnWaitMilliseconds) {
    toCallPage(
        context,
        generateUserModel(userInfo),
        ChannelType.AUDIO.getValue(),
        AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid()),
        callPstnWaitMilliseconds);
  }

  public static void toCallVideoPage(Context context, UserInfo userInfo) {
    toCallPage(
        context, generateUserModel(userInfo), ChannelType.VIDEO.getValue(), false, 15 * 1000);
  }

  private static UserModel generateUserModel(UserInfo userInfo) {
    UserModel userModel = new UserModel();
    userModel.imAccid = userInfo.getAccount();
    userModel.nickname = userInfo.getName();
    userModel.avatar = userInfo.getAvatar();
    Map<String, Object> extensionMap = userInfo.getExtensionMap();
    if (extensionMap != null) {
      if (extensionMap.get(AppParams.CALLED_USER_MOBILE) != null) {
        userModel.mobile = (String) extensionMap.get(AppParams.CALLED_USER_MOBILE);
      }
      userModel.callType = (int) extensionMap.get(AppParams.CALL_TYPE);
      if (extensionMap.get(AppParams.CALLED_AUDIO_URL) != null) {
        userModel.audioUrl = (String) extensionMap.get(AppParams.CALLED_AUDIO_URL);
      }
      if (extensionMap.get(AppParams.CALLED_VIDEO_URL) != null) {
        userModel.videoUrl = (String) extensionMap.get(AppParams.CALLED_VIDEO_URL);
      }
    }
    return userModel;
  }

  public static void toUserInfoPage(Context context, int position) {
    Intent intent = new Intent(context, UserInfoActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(AppParams.INDEX, position);
    context.startActivity(intent);
  }

  public static void toP2pPage(Context context, UserInfo userInfo, String content) {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_P2P_PAGE)
        .withParam(RouterConstant.CHAT_KRY, userInfo)
        .withParam(RouterConstant.CHAT_ID_KRY, userInfo.getAccount())
        .withParam("content", content)
        .withContext(context)
        .navigate();
  }
}
