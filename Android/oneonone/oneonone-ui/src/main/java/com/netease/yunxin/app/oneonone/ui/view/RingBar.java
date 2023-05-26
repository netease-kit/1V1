// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author: skd
 * @date 2018/6/26 @Desc GradientProgressBar
 */
public class RingBar extends View {
  private final float circleBorderWidth = dp2px(4);
  private final float circlePadding = dp2px(1);
  private Paint backgroundPaint;
  private int percent = 0;
  private Paint progressPaint;

  public RingBar(Context context) {
    super(context);
    init();
  }

  public RingBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RingBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    backgroundPaint = new Paint();
    backgroundPaint.setStyle(Paint.Style.STROKE);
    backgroundPaint.setAntiAlias(true);
    backgroundPaint.setColor(Color.WHITE);
    backgroundPaint.setStrokeWidth(circleBorderWidth);

    progressPaint = new Paint();
    progressPaint.setStyle(Paint.Style.STROKE);
    progressPaint.setAntiAlias(true);
    progressPaint.setStrokeWidth(circleBorderWidth);
    progressPaint.setStrokeCap(Paint.Cap.ROUND);
    progressPaint.setColor(Color.parseColor("#FB6E79"));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
    int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
    setMeasuredDimension(
        Math.min(measureWidth, measureHeight), Math.min(measureWidth, measureHeight));
  }

  @SuppressLint("DrawAllocation")
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    //1.绘制背景圆环
    RectF rectF =
        new RectF(
            circlePadding * 2,
            circlePadding * 2,
            getMeasuredWidth() - circlePadding * 2,
            getMeasuredHeight() - circlePadding * 2);
    //圆弧的起始角度-90度，就是从圆的顶部开始画,第二个参数代表h
    canvas.drawArc(rectF, -90, 360, false, backgroundPaint);
    //2.绘制圆环
    canvas.drawArc(
        new RectF(
            circlePadding * 2,
            circlePadding * 2,
            getMeasuredWidth() - circlePadding * 2,
            getMeasuredHeight() - circlePadding * 2),
        -90,
        (float) (percent / 100.0) * 360,
        false,
        progressPaint);
  }

  public void setPercent(int percent) {
    if (percent < 0) {
      percent = 0;
    } else if (percent > 100) {
      percent = 100;
    }
    this.percent = percent;
    if (Looper.myLooper() == Looper.getMainLooper()) {
      invalidate();
    } else {
      postInvalidate();
    }
  }

  private float dp2px(float dp) {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
  }
}
