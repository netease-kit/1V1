// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.utils.AudioInputManager;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUtil;
import com.netease.yunxin.app.oneonone.ui.view.RingBar;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.io.File;

public class AudioInputDialog extends Dialog {
  private static final String TAG = "AudioInputDialog";
  private View rootView;
  private TextView tvDesc;
  private ImageView ivDelete;
  private LottieAnimationView lottieAnimationView;
  private TextView chatMessageInputAudioTv;
  public final Rect flViewRect = new Rect();
  private String sessionID;
  private AudioInputManager audioInputManager;

  public AudioInputDialog(
      @NonNull Context context, String sessionID, AudioInputManager audioInputManager) {
    super(context);
    this.sessionID = sessionID;
    this.audioInputManager = audioInputManager;
    rootView =
        LayoutInflater.from(getContext()).inflate(R.layout.one_on_one_dialog_audio_input, null);
  }

  public AudioInputDialog(@NonNull Context context, int themeResId) {
    super(context, themeResId);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Window window = getWindow();
    if (window != null) {
      window.requestFeature(Window.FEATURE_NO_TITLE);
      setContentView(rootView);
      window.setBackgroundDrawable(new ColorDrawable(0x000000));
      window.setLayout(
          WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }
    setCanceledOnTouchOutside(false);
  }

  @Override
  public void show() {
    try {
      tvDesc = rootView.findViewById(R.id.tv_desc);
      ivDelete = rootView.findViewById(R.id.iv_delete);
      lottieAnimationView = rootView.findViewById(R.id.lottie_view);
      chatMessageInputAudioTv = rootView.findViewById(R.id.chat_message_input_audio_tv);
      RingBar rb = rootView.findViewById(R.id.rb);
      audioInputManager.setAudioInputCallback(
          new AudioInputManager.AudioInputCallback() {
            @Override
            public void onAudioRecordProgress(int percent) {
              rb.setPercent(percent);
              if (percent == 100) {
                if (isShowing()) {
                  dismiss();
                }
              }
            }

            @Override
            public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
              ChatUtil.sendAudioMessage(
                  sessionID,
                  SessionTypeEnum.P2P,
                  audioFile,
                  audioLength,
                  false,
                  new FetchCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                      ALog.i(TAG, "sendAudioMessage success");
                    }

                    @Override
                    public void onFailed(int code) {
                      ALog.e(TAG, "sendAudioMessage failed,code:" + code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                      ALog.e(TAG, "sendAudioMessage failed,exception:" + exception);
                    }
                  });
              if (isShowing()) {
                dismiss();
              }
            }

            @Override
            public void onRecordFail() {
              if (isShowing()) {
                dismiss();
              }
            }

            @Override
            public void onRecordCancel() {
              if (isShowing()) {
                dismiss();
              }
            }
          });
      FrameLayout fl = rootView.findViewById(R.id.fl);
      fl.post(
          () -> {
            int[] location = new int[2];
            fl.getLocationOnScreen(location);
            int viewLeft = location[0];
            int viewTop = location[1];
            int viewRight = viewLeft + fl.getWidth();
            int viewBottom = viewTop + fl.getHeight();
            flViewRect.left = viewLeft;
            flViewRect.top = viewTop;
            flViewRect.right = viewRight;
            flViewRect.bottom = viewBottom;
            ALog.i(TAG, "flViewRect:" + flViewRect);
          });
      super.show();
    } catch (WindowManager.BadTokenException e) {
      e.printStackTrace();
    }
  }

  public Rect getFlViewRect() {
    return flViewRect;
  }

  public void showCancelAudioSendUI(boolean showCancelState) {
    if (!isShowing()) {
      return;
    }
    if (showCancelState) {
      tvDesc.setText(getContext().getString(R.string.one_on_one_release_cancel));
      ivDelete.setVisibility(View.VISIBLE);
      lottieAnimationView.setVisibility(View.GONE);
    } else {
      tvDesc.setText(getContext().getString(R.string.one_on_one_release_send));
      ivDelete.setVisibility(View.GONE);
      lottieAnimationView.setVisibility(View.VISIBLE);
    }
  }

  public void refreshAudioInputLayout(int viewLeft, int viewTop, int viewRight, int viewBottom) {
    if (!isShowing()) {
      return;
    }
    ALog.i(
        TAG,
        "refreshAudioInputLayout,viewLeft:"
            + viewLeft
            + ",viewTop:"
            + viewTop
            + ",viewRight:"
            + viewRight
            + ",viewBottom:"
            + viewBottom);
    ConstraintLayout.LayoutParams layoutParams =
        new ConstraintLayout.LayoutParams(viewRight - viewLeft, viewBottom - viewTop);
    layoutParams.topToTop = R.id.rootview;
    layoutParams.leftToLeft = R.id.rootview;
    layoutParams.leftMargin = viewLeft;
    layoutParams.topMargin = viewTop - getStatusBarHeight();
    chatMessageInputAudioTv.setLayoutParams(layoutParams);
  }

  private int getStatusBarHeight() {
    Resources resources = getContext().getResources();
    int statusBarHeightResId = resources.getIdentifier("status_bar_height", "dimen", "android");
    return resources.getDimensionPixelSize(statusBarHeightResId);
  }
}
