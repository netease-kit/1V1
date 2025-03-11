// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.utils.LoginUtil;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.activity.BaseSampleLoginActivity;
import com.netease.yunxin.kit.entertainment.common.model.NemoAccount;

/** 登录页面 */
public class SampleLoginActivity extends BaseSampleLoginActivity {
  private static final String TAG = "SampleLoginActivity";

  public static void startLoginActivity(Context context) {
    hasStart = true;
    Intent intent = new Intent(context, SampleLoginActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  @Override
  public void login(NemoAccount nemoAccount) {
    LoginUtil.loginOneOnOne(
        this,
        nemoAccount,
        new LoginUtil.LoginOneOnOneCallback() {
          @Override
          public void onSuccess() {
            if (loadingDialog != null) {
              loadingDialog.dismiss();
            }
            gotoHomePage();
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ToastX.showShortToast(errorMsg);
            if (loadingDialog != null) {
              loadingDialog.dismiss();
            }
          }
        });
  }

  @Override
  public String getBaseUrl() {
    return AppConfig.getOneOnOneBaseUrl();
  }

  @Override
  public String getAppKey() {
    return AppConfig.getAppKey();
  }

  @Override
  public String getAppSecret() {
    return AppConfig.APP_SECRET;
  }

  @Override
  public int getIconResId() {
    return R.drawable.ic_launcher;
  }

  @Override
  public int getContentResId() {
    return R.string.sample_login_desc;
  }

  @Override
  public int getSceneType() {
    return 1;
  }

  private void gotoHomePage() {
    Intent intent = new Intent(this, HomeActivity.class);
    startActivity(intent);
    finish();
  }

  public static boolean shouldJumpToLoginActivity() {
    if (hasStart) {
      //登录页面已经存在，不需要重复进入登录页面。
      return false;
    }
    //已经登录过的情况下，当前状态不会走自动登录，需要重新登陆。
    StatusCode statusCode = NIMClient.getStatus();
    return statusCode.wontAutoLogin() || statusCode == StatusCode.LOGOUT;
  }
}
