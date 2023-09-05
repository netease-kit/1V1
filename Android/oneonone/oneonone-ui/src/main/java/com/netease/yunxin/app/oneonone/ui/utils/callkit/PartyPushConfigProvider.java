
// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils.callkit;

import com.netease.nimlib.sdk.avsignalling.model.SignallingPushConfig;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.utils.CallKitUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.XKitUtils;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.PushConfigProvider;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class PartyPushConfigProvider implements PushConfigProvider {
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
        true, CallKitUtil.getAppName(XKitUtils.getApplicationContext()), nickname, pushPayload);
  }
}
