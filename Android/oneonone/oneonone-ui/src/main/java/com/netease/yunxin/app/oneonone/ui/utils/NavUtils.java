// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.netease.yunxin.app.oneonone.ui.activity.UserInfoActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.kit.entertainment.common.activity.WebViewActivity;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.CallParam;

import org.json.JSONException;
import org.json.JSONObject;

public class NavUtils {

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
      extraInfo.putOpt(AppParams.CALLED_USER_NAME, userModel.nickname);
      extraInfo.putOpt(AppParams.CALLED_USER_MOBILE, userModel.mobile);
      extraInfo.putOpt(AppParams.CALLED_USER_AVATAR, userModel.avatar);
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

  public static void toUserInfoPage(Context context, int position) {
    Intent intent = new Intent(context, UserInfoActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(AppParams.INDEX, position);
    context.startActivity(intent);
  }
}
