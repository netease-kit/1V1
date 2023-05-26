// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import androidx.appcompat.app.AppCompatActivity;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.dialog.ReportDialog;

public class SettingPopupWindows extends PopupWindow {
  private static final String TAG = "SettingPopupWindows";
  private Context context;

  public SettingPopupWindows(Context context) {
    super(context);
    this.context = context;
    initViews();
  }

  private void initViews() {
    setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
    setOutsideTouchable(true);
    setFocusable(false);
    setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    View contentView =
        LayoutInflater.from(context).inflate(R.layout.popup_windows_setting, null, false);
    setContentView(contentView);
    contentView.setOnClickListener(
        v -> {
          dismiss();
          AppCompatActivity appCompatActivity = (AppCompatActivity) context;
          new ReportDialog().show(appCompatActivity.getSupportFragmentManager(), TAG);
        });
  }
}
