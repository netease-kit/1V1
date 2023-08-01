// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.faceunity.nama.R;
import com.faceunity.nama.control.FaceBeautyControlView;
import com.faceunity.nama.data.FaceUnityDataFactory;

/** DESC： Created on 2021/4/26 */
public class FaceUnityView extends LinearLayout {

  private Context mContext;

  public FaceUnityView(Context context) {
    super(context);
    mContext = context;
    init();
  }

  public FaceUnityView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    init();
  }

  public FaceUnityView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mContext = context;
    init();
  }

  private FaceUnityDataFactory mDataFactory;
  private FaceBeautyControlView mFaceBeautyControlView; //美颜菜单

  private void init() {
    LayoutInflater.from(mContext).inflate(R.layout.layout_faceunity, this);
    initView();
  }

  /**
   * 绑定数据工厂
   *
   * @param dataFactory FaceUnityDataFactory
   */
  public void bindDataFactory(FaceUnityDataFactory dataFactory) {
    mDataFactory = dataFactory;
    mFaceBeautyControlView.bindDataFactory(dataFactory.mFaceBeautyDataFactory);
    showFunction(0);
    mDataFactory.onFunctionSelected(0);
  }

  /** 初始化View */
  private void initView() {
    mFaceBeautyControlView = findViewById(R.id.control_beauty);
  }

  /**
   * UI菜单显示控制
   *
   * @param index Int
   */
  private void showFunction(int index) {
    mFaceBeautyControlView.setVisibility((index == 0) ? View.VISIBLE : View.GONE);
  }

  public void checkSkinBeautyTab() {
    mFaceBeautyControlView.checkSkinBeautyTab();
  }
}
