// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import org.json.JSONObject;

public class CommonRiskAttachment extends CustomAttachment {
  private static final String TAG = "CommonRiskAttachment";

  public CommonRiskAttachment() {
    super(OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE);
  }

  @Override
  protected void parseData(@Nullable JSONObject data) {
    ALog.i(TAG, "parseData data:" + data);
  }

  @Nullable
  @Override
  protected JSONObject packData() {
    return new JSONObject();
  }
}
