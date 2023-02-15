// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.home.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.databinding.RvItemHomeBinding;
import com.netease.yunxin.app.one2one.model.HomeItemModel;
import com.netease.yunxin.app.one2one.utils.AppGlobals;
import com.netease.yunxin.app.one2one.utils.DisplayUtils;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter {

  private ArrayList<HomeItemModel> list;
  private Activity activity;

  public HomeAdapter(Activity activity) {
    this.activity = activity;
  }

  public void bindData(ArrayList<HomeItemModel> list) {
    this.list = list;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    RvItemHomeBinding binding =
        RvItemHomeBinding.inflate(LayoutInflater.from(AppGlobals.getApplication()), parent, false);
    return new HomeHolder(activity, binding);
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    HomeItemModel homeItemModel = list.get(position);
    HomeHolder homeHolder = (HomeHolder) holder;
    homeHolder.bindData(homeItemModel, position);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public static class HomeHolder extends RecyclerView.ViewHolder {
    private RvItemHomeBinding binding;
    private Activity activity;

    public HomeHolder(Activity activity, @NonNull RvItemHomeBinding binding) {
      super(binding.getRoot());
      this.activity = activity;
      this.binding = binding;
    }

    @SuppressLint("SetTextI18n")
    public void bindData(HomeItemModel homeItemModel, int position) {
      Context context = AppGlobals.getApplication();
      ImageLoader.with(context)
          .roundedCorner(homeItemModel.imageUrl, (int) DisplayUtils.dp2px(4), binding.iv);
      binding.tv.setText(
          homeItemModel.nickName + " " + homeItemModel.age + context.getString(R.string.app_year));
      binding
          .getRoot()
          .setOnClickListener(
              new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  NavUtils.toUserInfoPage(activity, position);
                }
              });
    }
  }
}
