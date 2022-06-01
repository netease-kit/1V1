/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.constant.ErrorCode;
import com.netease.yunxin.app.one2one.utils.HighKeepAliveUtil;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.kit.login.model.UserInfo;

public class SplashActivity extends BaseActivity {

    @Override
    protected boolean needTransparentStatusBar() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }
        if (HighKeepAliveUtil.isHighKeepAliveOpen()){
            int code = HighKeepAliveUtil.openHighKeepAlive(this);
            if (code!= ErrorCode.SUCCESS){
                ToastUtils.showShort("open high keep alive feature failed,errorCode:"+code);
            }
        }
        AuthorManager.INSTANCE.autoLogin(true, new LoginCallback<UserInfo>() {
            @Override
            public void onSuccess(@Nullable UserInfo userInfo) {
                NavUtils.toMainPage(SplashActivity.this);
                finish();
            }

            @Override
            public void onError(int i, @NonNull String s) {
                NavUtils.toLoginHomePage(SplashActivity.this);
                finish();
            }
        });
    }
}
