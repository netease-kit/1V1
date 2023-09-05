// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.app.oneonone.ui.utils.CallKitUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.login.LoginCallback;
import com.netease.yunxin.kit.entertainment.common.model.NemoAccount;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import retrofit2.Call;
import retrofit2.Callback;

public class LoginUtil {
  private static final String TAG = "LoginUtil";

  public static void loginOneOnOne(
      Context context, NemoAccount nemoAccount, LoginOneOnOneCallback callback) {
    initOneOnOneHttpService(context, nemoAccount);
    HttpService.getInstance()
        .loginOneOnOne(
            new Callback<ModelResponse<User>>() {
              @Override
              public void onResponse(
                  Call<ModelResponse<User>> call,
                  retrofit2.Response<ModelResponse<User>> response) {
                if (response == null || response.body() == null || response.body().data == null) {
                  ALog.e(TAG, "loginOneOnOne failed");
                  if (callback != null) {
                    callback.onError(response.code(), "loginOneOnOne failed,userinfo is null");
                  }
                  return;
                }
                long rtcUid = response.body().data.getRtcUid();
                initCallKit(rtcUid);
                nemoAccount.rtcUid = rtcUid;
                loginIM(nemoAccount, callback);
              }

              @Override
              public void onFailure(Call<ModelResponse<User>> call, Throwable t) {
                ALog.e(TAG, "loginOneOnOne failed,t:" + t);
                UserInfoManager.clearUserInfo();
                if (callback != null) {
                  callback.onError(-1, "loginOneOnOne failed,t:" + t);
                }
              }
            });
  }

  private static void initOneOnOneHttpService(Context context, NemoAccount nemoAccount) {
    OneOnOneUI.getInstance()
        .initialize(context, AppConfig.getOneOnOneBaseUrl(), AppConfig.getAppKey());
    OneOnOneUI.getInstance().setChineseEnv(AppConfig.isChineseEnv());
    OneOnOneUI.getInstance().addHttpHeader(nemoAccount.userToken, nemoAccount.userUuid);
    OneOnOneUI.getInstance().setIsOversea(AppConfig.isOversea());
  }

  private static void loginIM(NemoAccount nemoAccount, LoginOneOnOneCallback oneOnOneCallback) {
    LoginInfo info = new LoginInfo(nemoAccount.userUuid, nemoAccount.imToken);
    LoginCallback<LoginInfo> callback =
        new LoginCallback<LoginInfo>() {
          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            // 常见错误码参考：https://doc.yunxin.163.com/messaging/docs/TM5NTk2Mzc?platform=server
            String toastStr = "";
            if (errorCode == 302) {
              ALog.e(TAG, "账号密码错误");
              toastStr = "账号密码错误";
              // your code
            } else if (errorCode == 408) {
              ALog.e(TAG, "网络超时");
              toastStr = "网络超时";
            } else if (errorCode == 415) {
              ALog.e(TAG, "网络出错");
              toastStr = "网络出错";
            } else {
              toastStr = "login IM failed,errorCode:" + errorCode + ",errorMsg:" + errorMsg;
              ALog.e(TAG, "login IM failed,errorCode:" + errorCode + ",errorMsg:" + errorMsg);
            }
            UserInfoManager.clearUserInfo();
            if (oneOnOneCallback != null) {
              oneOnOneCallback.onError(
                  errorCode, "im login failed,code:" + errorCode + ",errorMsg:" + toastStr);
            }
          }

          @Override
          public void onSuccess(LoginInfo param) {
            ALog.i(TAG, "login success");
            UserInfoManager.setUserInfo(
                nemoAccount.userUuid,
                nemoAccount.userToken,
                nemoAccount.imToken,
                nemoAccount.userName,
                nemoAccount.icon,
                nemoAccount.mobile);
            UserInfoManager.saveUserInfoToSp(nemoAccount);
            if (oneOnOneCallback != null) {
              oneOnOneCallback.onSuccess();
            }
          }
        };
    //执行手动登录
    IMKitClient.loginIM(info, callback);
  }

  private static void initCallKit(long rtcUid) {
    // 初始化呼叫组件
    CallKitUtil.initCallKit(rtcUid, "", AppConfig.getAppKey(), null);
  }

  public interface LoginOneOnOneCallback {
    void onSuccess();

    void onError(int errorCode, String errorMsg);
  }
}
