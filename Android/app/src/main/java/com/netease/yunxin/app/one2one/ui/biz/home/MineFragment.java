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

import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.DebugActivity;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.databinding.FragmentMineBinding;
import com.netease.yunxin.app.one2one.ui.view.MineItemView;
import com.netease.yunxin.app.one2one.utils.AppGlobals;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;

/**
 * mine tab fragment
 */
public class MineFragment extends Fragment {
    private FragmentMineBinding binding;
    private Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMineBinding.inflate(inflater, container, false);
        initView();
        initEvent();
        return binding.getRoot();
    }

    private void initEvent() {
        binding.tvNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toUserInfoEditPage(activity);
            }
        });

        binding.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toUserInfoEditPage(activity);
            }
        });

        binding.itemViewDiamond.setOnItemClickListener(new MineItemView.OnItemClickListener() {
            @Override
            public void onItemClick() {
                ToastUtils.showShort(R.string.todo_fuction_tips);
            }
        });
        binding.itemViewFollow.setOnItemClickListener(new MineItemView.OnItemClickListener() {
            @Override
            public void onItemClick() {
                ToastUtils.showShort(R.string.todo_fuction_tips);
            }
        });
        binding.itemViewLove.setOnItemClickListener(new MineItemView.OnItemClickListener() {
            @Override
            public void onItemClick() {
                ToastUtils.showShort(R.string.todo_fuction_tips);
            }
        });
        binding.itemViewBeauty.setOnItemClickListener(new MineItemView.OnItemClickListener() {
            @Override
            public void onItemClick() {
                NavUtils.toBeautySettingPage(activity);
            }
        });
        binding.itemViewSetting.setOnItemClickListener(new MineItemView.OnItemClickListener() {
            @Override
            public void onItemClick() {
                NavUtils.toSettingPage(activity);
            }
        });
    }

    private void initView() {
        ImmersionBar.with(this)
                .statusBarView(binding.statusBarHolder)
                .statusBarDarkFont(true)
                .init();
        if (UserInfoManager.getSelfUserInfo()!=null){
            binding.tvNickname.setText(UserInfoManager.getSelfUserInfo().getNickname());
            ImageLoader.with(AppGlobals.getApplication()).circleLoad(UserInfoManager.getSelfUserInfo().getAvatar(), binding.ivAvatar);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }

}
