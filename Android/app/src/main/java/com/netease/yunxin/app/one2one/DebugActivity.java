/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one;

import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.network.common.NetworkClient;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.constant.AppConstants;
import com.netease.yunxin.app.one2one.databinding.ActivityMain2Binding;
import com.netease.yunxin.app.one2one.http.HttpService;
import com.netease.yunxin.app.one2one.model.UserModel;
import com.netease.yunxin.app.one2one.utils.AccountAmountHelper;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;

import io.reactivex.observers.ResourceSingleObserver;

// TODO: 2022/4/26 调试页面，先别删
public class DebugActivity extends BaseActivity {
    private ActivityMain2Binding binding;
    private static final String TAG = "MainActivity";

    @Override
    protected boolean needTransparentStatusBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NetworkClient.getInstance().configAccessToken(UserInfoManager.getSelfAccessToken());
        binding.tvAccount.setText(UserInfoManager.getSelfNickname());
        binding.btnSendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toAppAboutPage(DebugActivity.this);
            }
        });
        binding.btnUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toUserInfoPage(DebugActivity.this, 0);
            }
        });
        binding.btnUserInfoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toUserInfoEditPage(DebugActivity.this);
            }
        });

        binding.btnPstnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toPSTNSettingPage(DebugActivity.this);
            }
        });

        binding.btnBindOtherAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toBindOtherPhonePage(DebugActivity.this);
            }
        });

        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toSettingPage(DebugActivity.this);
            }
        });

        binding.btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.toBrowsePage(DebugActivity.this, "", AppConstants.PRIVACY_POLICY);
            }
        });

        binding.btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoCallPage(ChannelType.VIDEO.getValue(),false);
            }
        });
        binding.btnAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoCallPage(ChannelType.AUDIO.getValue(),false);
            }
        });

        binding.btnAudioPstnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showShort(AccountAmountHelper.getPstnUsedDurationWithAccount(UserInfoManager.getSelfImAccid())+"");
                gotoCallPage(ChannelType.AUDIO.getValue(), AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid()));
            }
        });

    }

    private void gotoCallPage(int channelType,boolean needPstnCall) {

        String phoneNumber = binding.editText.getText().toString();
        HttpService.searchUserInfoWithPhoneNumber(phoneNumber).subscribe(new ResourceSingleObserver<BaseResponse<UserModel>>() {
            @Override
            public void onSuccess(BaseResponse<UserModel> userModelBaseResponse) {
                if (userModelBaseResponse.isSuccessful()) {
                    LogUtil.i(TAG, "phoneNumber:" + phoneNumber + ",imAccid:" + userModelBaseResponse.data.imAccid);
                    NavUtils.toCallPage(DebugActivity.this, userModelBaseResponse.data,channelType,needPstnCall);
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }
}