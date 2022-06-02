/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.userinfo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.databinding.ActivityUserInfoEditBinding;
import com.netease.yunxin.app.one2one.ui.view.StatusBarConfig;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;

public class UserInfoEditActivity extends BaseActivity {
    private ActivityUserInfoEditBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUserInfoEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initListeners();
    }
    private void initListeners() {
        ImageLoader.with(this).circleLoad(UserInfoManager.getSelfUserInfo().getAvatar(), binding.ivAvatar);
        binding.tvNickname.setText(UserInfoManager.getSelfUserInfo().getNickname());
    }

    @Override
    protected StatusBarConfig provideStatusBarConfig() {
        return new StatusBarConfig.Builder()
                .statusBarDarkFont(true)
                .statusBarColor(R.color.color_eff1f4)
                .fitsSystemWindow(true)
                .build();
    }
}
