// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.entertainment.common.fragment;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class BaseDialogFragment extends DialogFragment {
  public String TAG = getClass().getName();

  @Override
  public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(TAG) != null && manager.findFragmentByTag(TAG).isVisible()) {
      ((DialogFragment) manager.findFragmentByTag(TAG)).dismissAllowingStateLoss();
    }
    try {
      //在每个add事务前增加一个remove事务，防止连续的add
      manager.beginTransaction().remove(this).commit();
      super.show(manager, tag);
    } catch (Exception e) {
      //同一实例使用不同的tag会异常,这里捕获一下
      e.printStackTrace();
    }
  }

  @Override
  public void dismiss() {
    if (getFragmentManager() != null) {
      super.dismissAllowingStateLoss();
    }
  }
}
