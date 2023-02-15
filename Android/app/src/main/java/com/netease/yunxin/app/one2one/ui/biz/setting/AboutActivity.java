// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.setting;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.one2one.BuildConfig;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.databinding.ActivityAboutBinding;
import com.netease.yunxin.app.one2one.ui.view.StatusBarConfig;

public class AboutActivity extends BaseActivity {
  private ActivityAboutBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityAboutBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.aboutVersion.setText("V " + BuildConfig.VERSION_NAME);
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder()
        .statusBarDarkFont(true)
        .statusBarColor(android.R.color.white)
        .fitsSystemWindow(true)
        .build();
  }
}
