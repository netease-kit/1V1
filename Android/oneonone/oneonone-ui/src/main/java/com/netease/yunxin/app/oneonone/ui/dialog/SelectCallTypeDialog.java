// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.utils.ReportUtils;

public class SelectCallTypeDialog extends BottomBaseDialog {

  public static final String TAG_REPORT_PAGE = "page_1v1_list";
  private View rootView;

  public SelectCallTypeDialog(@NonNull Activity activity) {
    super(activity);
  }

  @Override
  protected void renderTopView(FrameLayout parent) {}

  @Override
  protected boolean enableTop() {
    return false;
  }

  @Override
  protected void renderBottomView(FrameLayout parent) {
    rootView =
        LayoutInflater.from(getContext())
            .inflate(R.layout.one_on_one_dialog_select_call_type, parent);
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
  }

  public void setDialogCallback(SelectCallTypeCallback dialogCallback) {
    this.dialogCallback = dialogCallback;
  }

  private SelectCallTypeCallback dialogCallback;

  public interface SelectCallTypeCallback {
    void onVideoCall(Dialog dialog);

    void onAudioCall(Dialog dialog);
  }
}
