// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.kit.common.utils.SizeUtils;

public class SharpView extends View {
  private int mWidth = 0;
  private int mHeight = 0;
  private Context mContext;

  public SharpView(Context context) {
    super(context);
    this.mContext = context;
    initView();
  }

  public SharpView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.mContext = context;
    initView();
  }

  private void initView() {
    mWidth = SizeUtils.dp2px(10);
    mHeight = SizeUtils.dp2px(6);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(mWidth, mHeight);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    //创建画笔
    Paint paint = new Paint();
    paint.setColor(ContextCompat.getColor(mContext, R.color.white));
    paint.setAntiAlias(true); //抗锯齿
    paint.setStyle(Paint.Style.FILL); //实线
    //创建路径
    Path path = new Path();
    path.moveTo(0, mHeight);
    path.lineTo(mWidth, mHeight);
    path.lineTo(mWidth / 2, 0);
    path.close(); //闭合路径
    //画在画布上
    canvas.drawPath(path, paint);
  }
}
