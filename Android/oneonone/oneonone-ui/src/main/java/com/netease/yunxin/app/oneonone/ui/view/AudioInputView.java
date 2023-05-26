// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.oneonone.ui.databinding.OneOnOneChatAudioInputViewBinding;

public class AudioInputView extends FrameLayout {
  public static final String TAG = "AudioInputView";
  private OneOnOneChatAudioInputViewBinding mBinding;

  public AudioInputView(@NonNull Context context) {
    this(context, null);
  }

  public AudioInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AudioInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    //    mBinding =
    //        OneOnOneChatAudioInputViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
    //    mBinding.chatMessageInputAudioTv.setOnTouchListener(
    //        new OnTouchListener() {
    //          @Override
    //          public boolean onTouch(View v, MotionEvent event) {
    //            if (event.getAction() == MotionEvent.ACTION_DOWN) {
    //              if (permissionRequest != null
    //                  && !permissionRequest.requestPermission(Manifest.permission.RECORD_AUDIO)) {
    //                return false;
    //              }
    //              initAudioRecord();
    //              startAudioRecord();
    //            } else if (event.getAction() == MotionEvent.ACTION_CANCEL
    //                || event.getAction() == MotionEvent.ACTION_UP) {
    //              endAudioRecord(isCancelled(v, event));
    //            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
    //              willCancelAudioRecord(isCancelled(v, event));
    //            }
    //            return true;
    //          }
    //        });
  }
}
