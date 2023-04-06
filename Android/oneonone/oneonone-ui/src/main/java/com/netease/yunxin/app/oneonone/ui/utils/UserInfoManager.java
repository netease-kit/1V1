// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.text.TextUtils;

import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.UserInfo;

public class UserInfoManager {
  private static String selfImAccid = "";
  private static String selfImToken = "";
  private static String selfImNickname = "";
  private static String selfImAvatar = "";
  private static String selfPhoneNumber = "";

  public static UserInfo getSelfUserInfo() {
    return AuthorManager.INSTANCE.getUserInfo();
  }

  public static String getSelfImNickname() {
    if (!TextUtils.isEmpty(selfImNickname)) {
      return selfImNickname;
    }
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getNickname();
  }

  public static String getSelfImAvatar() {
    if (!TextUtils.isEmpty(selfImAvatar)) {
      return selfImAvatar;
    }
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getAvatar();
  }

  public static String getSelfPhoneNumber() {
    if (!TextUtils.isEmpty(selfPhoneNumber)) {
      return selfPhoneNumber;
    }
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getUser();
  }

  public static String getSelfImAccid() {
    if (!TextUtils.isEmpty(selfImAccid)) {
      return selfImAccid;
    }
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getImAccid();
  }

  public static String getSelfAccessToken() {
    if (!TextUtils.isEmpty(selfImToken)) {
      return selfImToken;
    }
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getAccessToken();
  }

  public static String getSelfNickname() {
    if (!TextUtils.isEmpty(selfImNickname)) {
      return selfImNickname;
    }
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getNickname();
  }

  // 设置云信IM用户信息
  public static void setIMUserInfo(
      String imAccid, String imToken, String imNickname, String imAvatar, String phoneNumber) {
    selfImAccid = imAccid;
    selfImToken = imToken;
    selfImNickname = imNickname;
    selfImAvatar = imAvatar;
    selfPhoneNumber = phoneNumber;
  }
}
