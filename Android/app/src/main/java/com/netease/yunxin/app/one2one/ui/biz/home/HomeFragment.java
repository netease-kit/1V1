/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.home;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.one2one.constant.MockDatas;
import com.netease.yunxin.app.one2one.databinding.FragmentHomeBinding;
import com.netease.yunxin.app.one2one.model.HomeItemModel;
import com.netease.yunxin.app.one2one.ui.biz.home.adapter.HomeAdapter;
import com.netease.yunxin.app.one2one.ui.view.GridSpacingItemDecoration;
import com.netease.yunxin.app.one2one.utils.DisplayUtils;

import java.util.ArrayList;

/**
 * home tab fragment
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Activity activity;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        initView();
        initEvent();
        return binding.getRoot();
    }

    private void initView() {
        ImmersionBar.with(this)
                .statusBarView(binding.statusBarHolder)
                .statusBarDarkFont(true)
                .init();
        binding.recycleView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
        binding.recycleView.addItemDecoration(new GridSpacingItemDecoration(2, (int) DisplayUtils.dp2px(10), false, 0));
        HomeAdapter homeAdapter = new HomeAdapter(activity);
        ArrayList<HomeItemModel> list = new ArrayList<>();
        list.addAll(MockDatas.MOCK_USERS);
        homeAdapter.bindData(list);
        binding.recycleView.setAdapter(homeAdapter);
    }

    private void initEvent() {
        binding.ivQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new HelpNotesDialog().show(getChildFragmentManager(),"");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
