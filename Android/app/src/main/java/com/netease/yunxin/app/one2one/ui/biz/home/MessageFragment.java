// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.databinding.FragmentMessageBinding;

/** message tab fragment */
public class MessageFragment extends Fragment {
  private FragmentMessageBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMessageBinding.inflate(inflater, container, false);
    ImmersionBar.with(this).statusBarView(binding.statusBarHolder).statusBarDarkFont(true).init();
    binding.llSystemNotify.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ToastUtils.showShort(R.string.todo_fuction_tips2);
          }
        });
    return binding.getRoot();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
