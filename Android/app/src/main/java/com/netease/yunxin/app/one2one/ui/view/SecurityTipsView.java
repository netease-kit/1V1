// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.utils.DisplayUtils;

public class SecurityTipsView extends androidx.appcompat.widget.AppCompatTextView {
  private SecurityCountDownTimer securityCountDownTimer;

  public SecurityTipsView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public SecurityTipsView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public SecurityTipsView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    int dp12 = (int) DisplayUtils.dp2px(context, 12);
    setPadding(dp12, dp12, dp12, dp12);
    setBackgroundResource(R.drawable.bg_security_tips);
    setTextColor(Color.WHITE);
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0F);
  }

  /** @param duration 持续展示时长，单位毫秒 */
  public void show(int duration) {
    setVisibility(VISIBLE);
    if (duration > 0) {
      if (securityCountDownTimer == null) {
        securityCountDownTimer = new SecurityCountDownTimer(duration, 1000);
      }
      securityCountDownTimer.start();
    }
  }

  public void hide() {
    setVisibility(GONE);
  }

  private final class SecurityCountDownTimer extends CountDownTimer {

    public SecurityCountDownTimer(long millisInFuture, long countDownInterval) {
      super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {}

    @Override
    public void onFinish() {
      hide();
    }
  }
}
