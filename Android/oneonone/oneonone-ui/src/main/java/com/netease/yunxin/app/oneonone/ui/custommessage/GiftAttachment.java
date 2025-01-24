// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import org.json.JSONObject;

public class GiftAttachment extends CustomAttachment {

  private static final String TAG = "GiftAttachment";
  private static final String GIFT_ID = "giftId";
  private static final String GIFT_COUNT = "giftCount";
  private static final String TARGET_USER_UUID = "targetUserUuid";

  private int giftId;

  private int giftCount;

  private String targetUserUuid;

  public GiftAttachment() {
    super(OneOnOneChatCustomMessageType.SEND_GIFT_TYPE);
  }

  @Override
  protected void parseData(@Nullable JSONObject data) {
    ALog.i(TAG, "parseData data:" + data);

    try {
      this.giftId = data.getInt(GIFT_ID);
      this.giftCount = data.getInt(GIFT_COUNT);
      this.targetUserUuid = data.getString(TARGET_USER_UUID);
    } catch (Exception e) {
      ALog.e(TAG, "parseData exception e = " + e.getMessage());
    }
  }

  @Nullable
  @Override
  protected JSONObject packData() {
    return new JSONObject();
  }

  public int getGiftId() {
    return giftId;
  }

  public int getGiftCount() {
    return giftCount;
  }

  public String getTargetUserUuid() {
    return targetUserUuid;
  }

  @Nullable
  @Override
  public String getContent() {
    return OneOnOneUI.getInstance().getContext().getString(R.string.one_on_one_message_gift);
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
