// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.yunxin.app.oneonone.BuildConfig;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.databinding.ActivitySampleLoginBinding;
import com.netease.yunxin.app.oneonone.utils.LoginUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.LoadingDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.activity.BasePartyActivity;
import com.netease.yunxin.kit.entertainment.common.http.ECHttpService;
import com.netease.yunxin.kit.entertainment.common.model.ECModelResponse;
import com.netease.yunxin.kit.entertainment.common.model.NemoAccount;
import retrofit2.Call;
import retrofit2.Callback;

/** 登录页面 */
public class SampleLoginActivity extends BasePartyActivity {
  private static final String TAG = "SampleLoginActivity";
  private ActivitySampleLoginBinding binding;
  private static Boolean hasStart = false;
  private LoadingDialog loadingDialog;

  public static void startLoginActivity(Context context) {
    hasStart = true;
    Intent intent = new Intent(context, SampleLoginActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  @Override
  protected View getRootView() {
    hasStart = true;
    binding = ActivitySampleLoginBinding.inflate(getLayoutInflater());
    binding.login.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            createAccountThenLogin();
          }
        });
    return binding.getRoot();
  }

  private void createAccountThenLogin() {
    loadingDialog = new LoadingDialog(this);
    loadingDialog.setLoadingText(getString(R.string.logining));
    loadingDialog.show();
    // 先创建账号再登录1v1再登录云信IM
    createAccount(
        AppConfig.getAppKey(),
        AppConfig.APP_SECRET,
        1,
        BuildConfig.VERSION_NAME,
        new Callback<ECModelResponse<NemoAccount>>() {
          @Override
          public void onResponse(
              Call<ECModelResponse<NemoAccount>> call,
              retrofit2.Response<ECModelResponse<NemoAccount>> response) {
            if (response.body() != null) {
              NemoAccount nemoAccount = response.body().data;
              if (nemoAccount != null) {
                loginOneOnOne(nemoAccount);
              } else {
                ToastX.showShortToast("createAccountThenLogin failed,account is null");
                ALog.e(TAG, "createAccountThenLogin failed,account is null");
              }
            }
          }

          @Override
          public void onFailure(Call<ECModelResponse<NemoAccount>> call, Throwable t) {
            ToastX.showShortToast("createAccountThenLogin failed,t:" + t);
            ALog.e(TAG, "createAccountThenLogin failed,exception:" + t);
          }
        });
  }

  private void loginOneOnOne(NemoAccount nemoAccount) {
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

  private void createAccount(
      String appKey,
      String appSecret,
      int sceneType,
      String versionCode,
      Callback<ECModelResponse<NemoAccount>> callback) {
    ECHttpService.getInstance().initialize(this, AppConfig.getOneOnOneBaseUrl());
    ECHttpService.getInstance().addHeader("appkey", appKey);
    ECHttpService.getInstance().addHeader("AppSecret", appSecret);
    ECHttpService.getInstance().addHeader("versionCode", versionCode);
    ECHttpService.getInstance().createAccount(sceneType, callback);
  }

  private void gotoHomePage() {
    Intent intent = new Intent(this, HomeActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    hasStart = false;
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
