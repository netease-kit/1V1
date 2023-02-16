// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.app.one2one.constant.ErrorCode;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.UserInfo;
import java.util.Collections;
import java.util.List;

public class UserInfoManager {

  public static UserInfo getSelfUserInfo() {
    return AuthorManager.INSTANCE.getUserInfo();
  }

  public static String getSelfImAccid() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getImAccid();
  }

  public static String getSelfAccessToken() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().getAccessToken();
  }

  public static String getSelfNickname() {
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      LogUtil.d("UserInfoManager", "userInfo is null");
      return "";
    }
    return AuthorManager.INSTANCE.getUserInfo().toJson().toString();
  }

  public static void getUserInfoByImAccId(String imAccId, NECallback<NimUserInfo> callback) {
    NIMClient.getService(UserService.class)
        .fetchUserInfo(Collections.singletonList(imAccId))
        .setCallback(
            new RequestCallback<List<NimUserInfo>>() {
              @Override
              public void onSuccess(List<NimUserInfo> param) {
                if (param != null && !param.isEmpty()) {
                  NimUserInfo nimUserInfo = param.get(0);
                  callback.onSuccess(nimUserInfo);
                }
              }

              @Override
              public void onFailed(int code) {
                callback.onError(code, "Failed to fetch userInfo,errorCode=" + code);
              }

              @Override
              public void onException(Throwable exception) {
                callback.onError(
                    ErrorCode.ERROR, "Failed to fetch userInfo,exception=" + exception);
              }
            });
  };
}
