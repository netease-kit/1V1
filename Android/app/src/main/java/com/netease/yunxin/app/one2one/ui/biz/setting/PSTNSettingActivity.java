// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.setting;

import android.os.Bundle;
import android.widget.CompoundButton;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.constant.ErrorCode;
import com.netease.yunxin.app.one2one.databinding.ActivityPstnSettingBinding;
import com.netease.yunxin.app.one2one.ui.view.StatusBarConfig;
import com.netease.yunxin.app.one2one.utils.HighKeepAliveUtil;

public class PSTNSettingActivity extends BaseActivity {
  private ActivityPstnSettingBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityPstnSettingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initView();
  }

  private void initView() {
    binding.switchButton.setChecked(HighKeepAliveUtil.isHighKeepAliveOpen());
    binding.switchButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
              int code = HighKeepAliveUtil.openHighKeepAlive(PSTNSettingActivity.this);
              if (code != ErrorCode.SUCCESS) {
                ToastUtils.showShort("open high keep alive feature failed,errorCode:" + code);
                compoundButton.setChecked(false);
                new AlertDialog.Builder(PSTNSettingActivity.this)
                    .setTitle(R.string.tips)
                    .setMessage(R.string.app_notification_tips)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(
                        R.string.sure,
                        (dialog, which) -> {
                          HighKeepAliveUtil.requestNotifyPermission(PSTNSettingActivity.this);
                          dialog.dismiss();
                        })
                    .create()
                    .show();
              }
            } else {
              HighKeepAliveUtil.closeHighKeepAlive(PSTNSettingActivity.this);
            }
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
