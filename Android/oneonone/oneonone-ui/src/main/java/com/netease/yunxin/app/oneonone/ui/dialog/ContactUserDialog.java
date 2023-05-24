// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.dialog;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.kit.common.ui.dialog.BaseDialog;
import com.netease.yunxin.kit.entertainment.common.utils.ReportUtils;

public class ContactUserDialog extends BaseDialog {

  public static final String TAG_REPORT_PAGE = "page_1v1_list";
  public static final String TAG = "ContactUserDialog";
  private View rootView;

  @Override
  protected void setStyle() {
    setStyle(STYLE_NORMAL, R.style.TransCommonDialogTheme);
  }

  @Override
  protected void initParams() {
    Window window = getDialog().getWindow();
    if (window != null) {
      WindowManager.LayoutParams params = window.getAttributes();
      params.gravity = Gravity.BOTTOM;
      params.width = ViewGroup.LayoutParams.MATCH_PARENT;
      params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      window.setAttributes(params);
    }
    setCancelable(true);
  }

  public void setDialogCallback(SelectCallTypeCallback dialogCallback) {
    this.dialogCallback = dialogCallback;
  }

  private SelectCallTypeCallback dialogCallback;

  @Nullable
  @Override
  protected View getRootView(
      @NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup) {
    rootView =
        LayoutInflater.from(getContext())
            .inflate(R.layout.one_on_one_dialog_contact_user, viewGroup);
    rootView
        .findViewById(R.id.rl_audio_call)
        .setOnClickListener(
            v -> {
              if (dialogCallback != null) {
                ReportUtils.report(getContext(), TAG_REPORT_PAGE, "1v1_audiocall");
                dialogCallback.onAudioCall(this);
              }
            });
    rootView
        .findViewById(R.id.rl_video_call)
        .setOnClickListener(
            v -> {
              if (dialogCallback != null) {
                ReportUtils.report(getContext(), TAG_REPORT_PAGE, "1v1_videocall");
                dialogCallback.onVideoCall(this);
              }
            });
    rootView
        .findViewById(R.id.ll_accost)
        .setOnClickListener(
            v -> {
              if (dialogCallback != null) {
                dialogCallback.onAccost(this);
              }
            });
    rootView
        .findViewById(R.id.ll_private_letter)
        .setOnClickListener(
            v -> {
              if (dialogCallback != null) {
                dialogCallback.onPrivateLetter(this);
              }
            });
    return rootView;
  }

  public interface SelectCallTypeCallback {
    void onAccost(ContactUserDialog dialog);

    void onPrivateLetter(ContactUserDialog dialog);

    void onVideoCall(ContactUserDialog dialog);

    void onAudioCall(ContactUserDialog dialog);
  }
}
