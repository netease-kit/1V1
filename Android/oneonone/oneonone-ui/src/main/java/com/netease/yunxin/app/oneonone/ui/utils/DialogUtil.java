// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import androidx.appcompat.app.AppCompatActivity;
import com.netease.yunxin.kit.common.ui.dialog.CommonAlertDialog;

public class DialogUtil {
  public static void showConfirmDialog(AppCompatActivity activity, String title) {
    if (activity.isFinishing() || activity.getSupportFragmentManager().isDestroyed()) {
      return;
    }
    CommonAlertDialog commonDialog = new CommonAlertDialog();
    commonDialog
        .setTitleStr(title)
        .setPositiveStr(
            activity.getString(com.netease.yunxin.app.oneonone.ui.R.string.one_on_one_confirm))
        .setConfirmListener(() -> {})
        .show(activity.getSupportFragmentManager());
  }
}
