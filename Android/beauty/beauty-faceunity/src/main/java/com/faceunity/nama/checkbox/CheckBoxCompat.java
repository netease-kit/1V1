// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.checkbox;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatCheckBox;

/**
 * 解决 RadioButton 在 Android4.4 调用setButtonDrawable(null) 和 XML 设置 android:button="@null"无效的问题
 *
 * @author Richie on 2020.05.18
 */
public class CheckBoxCompat extends AppCompatCheckBox {

  public CheckBoxCompat(Context context) {
    super(context);
    init();
  }

  public CheckBoxCompat(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CheckBoxCompat(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setButtonDrawable(new StateListDrawable());
  }
}
