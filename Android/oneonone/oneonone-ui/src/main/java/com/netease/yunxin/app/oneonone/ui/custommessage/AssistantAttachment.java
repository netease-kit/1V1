// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import android.text.Html;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import org.json.JSONObject;

public class AssistantAttachment extends CustomAttachment {

  private static final String TAG = "AssistantAttachment";
  private static final String MSG = "msg";

  private String msg;

  public AssistantAttachment() {
    super(OneOnOneChatCustomMessageType.ASSISTANT_MESSAGE_TYPE);
  }

  @Override
  protected void parseData(@Nullable JSONObject data) {
    ALog.i(TAG, "parseData data:" + data);
    msg = data.optString(MSG);
  }

  @Nullable
  @Override
  protected JSONObject packData() {
    return new JSONObject();
  }

  public String getMsg() {
    return msg;
  }

  @Nullable
  @Override
  public String getContent() {
    return Html.fromHtml(msg).toString();
  }

  @NonNull
  @Override
  public String toJsonStr() {
    try {
      JSONObject map = new JSONObject();
      map.put("type", getCustomType());
      map.put("data", packData());
      return map.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }
}
