// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ScreenUtils;
import com.netease.yunxin.app.oneonone.ui.databinding.RvItemUserInfoBinding;
import com.netease.yunxin.app.oneonone.ui.model.UserInfoDetailModel;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.kit.common.image.ImageLoader;

public class UserInfoAdapter extends RecyclerView.Adapter {
  private static final String TAG = "UserInfoAdapter";
  private UserInfoDetailModel model;

  @SuppressLint("NotifyDataSetChanged")
  public void bindData(UserInfoDetailModel model) {
    this.model = model;
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    RvItemUserInfoBinding binding =
        RvItemUserInfoBinding.inflate(
            LayoutInflater.from(AppGlobals.getApplication()), parent, false);
    return new UserInfoHolder(binding);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    UserInfoHolder userInfoHolder = (UserInfoHolder) holder;
    userInfoHolder.bindData(model);
  }

  @Override
  public int getItemCount() {
    return 1;
  }

  public static class UserInfoHolder extends RecyclerView.ViewHolder {
    private RvItemUserInfoBinding binding;

    public UserInfoHolder(@NonNull RvItemUserInfoBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }

    @SuppressLint("SetTextI18n")
    public void bindData(UserInfoDetailModel userInfoDetailModel) {
      if (userInfoDetailModel == null) {
        return;
      }
      LogUtil.i(TAG, "bindData,userInfoDetailModel:" + userInfoDetailModel);
      Context context = AppGlobals.getApplication();
      ViewGroup.LayoutParams layoutParams = binding.ivBg.getLayoutParams();
      layoutParams.height = ScreenUtils.getScreenWidth();
      binding.ivBg.setLayoutParams(layoutParams);
      ImageLoader.with(context).commonLoad(userInfoDetailModel.bgUrl, binding.ivBg);
      binding.tvNickname.setText(userInfoDetailModel.nickname);
      binding.tvDesc.setText(userInfoDetailModel.desc);
      binding.albumView.bindData(userInfoDetailModel.albums);
      binding.tvAgeValue.setText(userInfoDetailModel.age);
      binding.tvHeightValue.setText(userInfoDetailModel.height);
      binding.tvWeightValue.setText(userInfoDetailModel.weight);
    }
  }
}
