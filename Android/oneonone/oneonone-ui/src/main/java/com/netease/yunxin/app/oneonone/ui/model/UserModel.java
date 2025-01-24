// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.model;

import android.text.TextUtils;
import java.io.Serializable;
import java.util.Map;

/** 业务用户数据 */
public final class UserModel implements Serializable {
  public String mobile; //String  登录的手机号
  public String accessToken; //String  登录令牌，重新生成的新令牌，过期时间重新计算
  public String account; //long  IM账号
  public String token; //String  IM令牌，重新生成的新令牌
  public long avRoomUid; //String  音视频房间内成员编号
  public String avatar; //String  头像地址
  public String nickname; //String  昵称
  public String audioUrl;
  public String videoUrl;
  public int callType; // 0表示正常呼叫，1表示呼叫虚拟人
  public Map<String, Object> extensionMap; // 扩展字段

  public UserModel() {}

  public UserModel(String account, String nickname, String avatar) {
    this.account = account;
    this.nickname = nickname;
    this.avatar = avatar;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public Map<String, Object> getExtensionMap() {
    return extensionMap;
  }

  public void setExtensionMap(Map<String, Object> extensionMap) {
    this.extensionMap = extensionMap;
  }

  public String getAccount() {
    return account;
  }

  public String getNickname() {
    return nickname;
  }

  public String getAvatar() {
    return avatar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserModel userModel = (UserModel) o;
    return TextUtils.equals(this.mobile, userModel.mobile);
  }
}
