// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.setting;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.constant.AppConfig;
import com.netease.yunxin.app.one2one.databinding.ActivitySettingBinding;
import com.netease.yunxin.app.one2one.ui.view.SettingItemView;
import com.netease.yunxin.app.one2one.ui.view.StatusBarConfig;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;

/** setting page */
public class SettingActivity extends BaseActivity {
  private ActivitySettingBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySettingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    handlePSTNTips();
    initEvent();
  }

  private void handlePSTNTips() {
    if (!AppConfig.isChineseEnv()) {
      binding.itemViewPstn.setVisibility(View.GONE);
      binding.linePstn.setVisibility(View.GONE);
    }
  }

  private void initEvent() {
    binding.itemViewPrivacyPolicy.setOnItemClickListener(
        new SettingItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toBrowsePage(
                SettingActivity.this,
                SettingActivity.this.getString(R.string.privacy_policy),
                AppConfig.getPrivacyPolicyUrl());
          }
        });
    binding.itemViewUserAgreement.setOnItemClickListener(
        new SettingItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toBrowsePage(
                SettingActivity.this,
                SettingActivity.this.getString(R.string.user_agreement),
                AppConfig.getUserAgreementUrl());
          }
        });
    binding.itemViewAboutUs.setOnItemClickListener(
        new SettingItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toAppAboutPage(SettingActivity.this);
          }
        });
    binding.itemViewBindOtherAccount.setOnItemClickListener(
        new SettingItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toBindOtherPhonePage(SettingActivity.this);
          }
        });
    binding.itemViewPstn.setOnItemClickListener(
        new SettingItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toPSTNSettingPage(SettingActivity.this);
          }
        });
    binding.tvLogout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            AuthorManager.INSTANCE.logout(
                new LoginCallback<Void>() {
                  @Override
                  public void onSuccess(@Nullable Void unused) {
                    finish();
                  }

                  @Override
                  public void onError(int i, @NonNull String s) {}
                });
          }
        });
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder()
        .statusBarDarkFont(true)
        .statusBarColor(R.color.color_eff1f4)
        .fitsSystemWindow(true)
        .build();
  }
}
