// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

import androidx.annotation.NonNull;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class TryVideoCallMessageAttachment extends CustomAttachment {
  private static final String TAG = "TryVideoCallMessageAttachment";
  private static final String KEY_TARGET_ACCOUNT = "target_account";
  private static final String KEY_TARGET_NICKNAME = "target_nickname";
  private static final String KEY_TARGET_AVATAR = "target_avatar";
  private static final String KEY_TARGET_MOBILE = "target_mobile";
  private static final String KEY_CALL_TYPE = "call_type";
  private static final String KEY_TARGET_VIDEO_URL = "target_video_url";
  private String targetAccount;
  private String targetNickname;
  private String targetAvatar;
  private String targetMobile;
  private int callType;
  private String videoUrl;

  public TryVideoCallMessageAttachment() {
    super(OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE);
  }

  public TryVideoCallMessageAttachment(UserModel userInfo) {
    this();
    this.targetAccount = userInfo.getAccount();
    this.targetNickname = userInfo.getNickname();
    this.targetAvatar = userInfo.getAvatar();
    this.targetMobile = userInfo.getMobile();
    Map<String, Object> extensionMap = userInfo.getExtensionMap();
    if (extensionMap != null) {
      this.callType = (int) extensionMap.get(AppParams.CALL_TYPE);
      this.videoUrl = (String) extensionMap.get(AppParams.CALLED_VIDEO_URL);
    }
  }

  @Override
  protected void parseData(JSONObject data) {
    ALog.i(TAG, "parseData data:" + data);
    try {
      this.targetAccount = data.getString(KEY_TARGET_ACCOUNT);
      this.targetNickname = data.getString(KEY_TARGET_NICKNAME);
      this.targetAvatar = data.getString(KEY_TARGET_AVATAR);
      this.targetMobile = data.getString(KEY_TARGET_MOBILE);
      this.callType = data.getInt(KEY_CALL_TYPE);
      this.videoUrl = data.getString(KEY_TARGET_VIDEO_URL);
    } catch (Exception exception) {
      ALog.e(TAG, "parseData exception:" + exception);
    }
  }

  @Override
  protected JSONObject packData() {
    JSONObject data = new JSONObject();
    try {
      data.put(KEY_TARGET_ACCOUNT, targetAccount);
      data.put(KEY_TARGET_NICKNAME, targetNickname);
      data.put(KEY_TARGET_AVATAR, targetAvatar);
      data.put(KEY_TARGET_MOBILE, targetMobile);
      data.put(KEY_CALL_TYPE, callType);
      data.put(KEY_TARGET_VIDEO_URL, videoUrl);
    } catch (Exception exception) {
      ALog.e(TAG, "packData exception:" + exception);
    }
    return data;
  }

  public UserModel getUserInfo() {
    UserModel userInfo = new UserModel(targetAccount, targetNickname, targetAvatar);
    userInfo.setMobile(targetMobile);
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put(AppParams.CALLED_USER_MOBILE, targetMobile);
    hashMap.put(AppParams.CALL_TYPE, callType);
    hashMap.put(AppParams.CALLED_VIDEO_URL, videoUrl);
    userInfo.setExtensionMap(hashMap);
    return userInfo;
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
