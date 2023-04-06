// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.callkit;

import com.netease.nimlib.sdk.avsignalling.model.SignallingPushConfig;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.utils.Utils;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.PushConfigProvider;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class RtcPushConfigProvider implements PushConfigProvider {
  private static final String TAG = "PartyPushConfigProvider";

  @Override
  public SignallingPushConfig providePushConfig(InvitedInfo invitedInfo) {
    String nickname = invitedInfo.currentAccId;
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(invitedInfo.attachment);
      nickname = jsonObject.optString(AppParams.CALLER_USER_NAME);
    } catch (JSONException e) {
      e.printStackTrace();
      ALog.e(TAG, "e:" + e);
    }
    HashMap<String, Object> pushPayload = new HashMap<>();
    pushPayload.put("attachment", invitedInfo.attachment);
    return new SignallingPushConfig(
        true, Utils.getApp().getString(R.string.one_on_one_app_name), nickname, pushPayload);
  }
}
