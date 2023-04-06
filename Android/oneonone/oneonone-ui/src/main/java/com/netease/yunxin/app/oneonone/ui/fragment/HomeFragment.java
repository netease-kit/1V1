// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.databinding.FragmentHomeBinding;
import com.netease.yunxin.app.oneonone.ui.fragment.adapter.HomeAdapter;
import com.netease.yunxin.app.oneonone.ui.view.FooterView;
import com.netease.yunxin.app.oneonone.ui.view.HeaderView;
import com.netease.yunxin.app.oneonone.ui.viewmodel.HomeViewModel;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

/** home tab fragment */
public class HomeFragment extends Fragment implements OnRefreshListener, OnLoadMoreListener {
  private FragmentHomeBinding binding;
  private AppCompatActivity activity;
  private HomeViewModel homeViewModel;
  private int pageNum = 0;
  private int pageSize = 20;
  private HomeAdapter homeAdapter;
  private boolean haveMore = false;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (AppCompatActivity) context;
  }

  @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    initView();
    observerData();
    return binding.getRoot();
  }

  @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
  private void observerData() {
    homeViewModel
        .getUserListData()
        .observe(
            requireActivity(),
            listBooleanPair -> {
              haveMore = listBooleanPair.second;
              homeAdapter.bindData(listBooleanPair.first);
              if (!haveMore) {
                binding.refreshLayout.finishLoadMoreWithNoMoreData();
              } else {
                binding.refreshLayout.finishLoadMore(true);
              }
              if (listBooleanPair.first.isEmpty()) {
                binding.llEmpty.setVisibility(View.VISIBLE);
                binding.recycleView.setVisibility(View.GONE);
              } else {
                binding.llEmpty.setVisibility(View.GONE);
                binding.recycleView.setVisibility(View.VISIBLE);
              }
            });
  }

  private void initView() {
    Context context = requireActivity();
    ImmersionBar.with(this).statusBarView(binding.statusBarHolder).statusBarDarkFont(true).init();
    binding.refreshLayout.setRefreshHeader(new HeaderView(context));
    binding.refreshLayout.setRefreshFooter(new FooterView(context));
    binding.refreshLayout.setOnRefreshListener(this);
    binding.refreshLayout.setOnLoadMoreListener(this);
    GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), 2);
    binding.recycleView.addItemDecoration(new MyItemDecoration());
    binding.recycleView.setLayoutManager(gridLayoutManager);
    homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    homeViewModel.getUserList(pageNum, pageSize);
    homeAdapter = new HomeAdapter(activity);
    binding.recycleView.setAdapter(homeAdapter);
    if (!OneOnOneUI.getInstance().isChineseEnv()) {
      binding.ivQuestion.setVisibility(View.GONE);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
    loadMore();
  }

  private void loadMore() {
    if (!haveMore) {
      binding.refreshLayout.finishLoadMoreWithNoMoreData();
    } else {
      pageNum++;
      homeViewModel.getUserList(pageNum, pageSize);
    }
  }

  @Override
  public void onRefresh(@NonNull RefreshLayout refreshLayout) {
    refresh();
  }

  private void refresh() {
    pageNum = 0;
    homeViewModel.getUserList(pageNum, pageSize);
  }

  static class MyItemDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int pixel8 = SizeUtils.dp2px(8f);
      int pixel4 = SizeUtils.dp2px(4f);
      int position = parent.getChildAdapterPosition(view);
      int left;
      int right;
      if (position % 2 == 0) {
        left = pixel8;
        right = pixel4;
      } else {
        left = pixel4;
        right = pixel8;
      }
      outRect.set(left, pixel4, right, pixel4);
    }
  }
}
