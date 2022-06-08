/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.userinfo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.constant.AppParams;
import com.netease.yunxin.app.one2one.databinding.ActivityUserInfoBinding;
import com.netease.yunxin.app.one2one.http.HttpService;
import com.netease.yunxin.app.one2one.model.UserInfoDetailModel;
import com.netease.yunxin.app.one2one.model.UserModel;
import com.netease.yunxin.app.one2one.utils.AccountAmountHelper;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.app.one2one.utils.PhoneBindUtil;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;

import io.reactivex.observers.ResourceSingleObserver;

public class UserInfoActivity extends BaseActivity {
    private ActivityUserInfoBinding binding;
    private static final String TAG = "UserInfoActivity";
    private int index;

    @Override
    protected boolean needTransparentStatusBar() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        parseBundle();
        initView();
        initEvent();
    }

    private void parseBundle() {
        index = getIntent().getIntExtra(AppParams.INDEX, 0);
    }

    private void initView() {
        ImmersionBar.with(this)
                .statusBarView(binding.statusBarHolder)
                .statusBarDarkFont(true)
                .init();
        UserInfoAdapter userInfoAdapter = new UserInfoAdapter();
        binding.recycleView.setAdapter(userInfoAdapter);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        UserInfoViewModel viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
        viewModel.fetchData(index);
        viewModel.getDetail().observe(this, new Observer<UserInfoDetailModel>() {
            @Override
            public void onChanged(UserInfoDetailModel userInfoDetailModel) {
                userInfoAdapter.bindData(userInfoDetailModel);
            }
        });
    }

    private void initEvent() {
        binding.rlAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.rlAudioCall.setEnabled(false);
                binding.rlVideoCall.setEnabled(false);
                handleCallLogic(ChannelType.AUDIO.getValue());
            }
        });
        binding.rlVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.rlAudioCall.setEnabled(false);
                binding.rlVideoCall.setEnabled(false);
                handleCallLogic(ChannelType.VIDEO.getValue());
            }
        });
    }

    private void handleCallLogic(int channelType) {
        if (TextUtils.isEmpty(PhoneBindUtil.getLastBindPhoneNumber())) {
            ToastUtils.showLong(R.string.please_input_phone_number_in_setting_page);
            binding.rlAudioCall.setEnabled(true);
            binding.rlVideoCall.setEnabled(true);
            return;
        }
        if (channelType==ChannelType.AUDIO.getValue()){
            gotoCallPage(channelType, AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid()));
        }else {
            gotoCallPage(channelType, false);
        }
    }

    private void gotoCallPage(int channelType, boolean needPstnCall) {
        String phoneNumber = PhoneBindUtil.getLastBindPhoneNumber();
        HttpService.searchUserInfoWithPhoneNumber(phoneNumber).subscribe(new ResourceSingleObserver<BaseResponse<UserModel>>() {
            @Override
            public void onSuccess(BaseResponse<UserModel> userModelBaseResponse) {
                if (userModelBaseResponse.isSuccessful()&&userModelBaseResponse.data!=null) {
                    LogUtil.i(TAG, "phoneNumber:" + phoneNumber + ",imAccid:" + userModelBaseResponse.data.imAccid);
                    NavUtils.toCallPage(UserInfoActivity.this, userModelBaseResponse.data, channelType, needPstnCall);
                } else {
                    LogUtil.e(TAG, "call failed errorMsg:" + userModelBaseResponse.msg);
                    ToastUtils.showShort(R.string.phonenumber_error_tips);
                }
                binding.rlAudioCall.setEnabled(true);
                binding.rlVideoCall.setEnabled(true);
            }

            @Override
            public void onError(Throwable e) {
                LogUtil.e(TAG, "call failed errorMsg:" + e);
                ToastUtils.showShort(R.string.network_error);
                binding.rlAudioCall.setEnabled(true);
                binding.rlVideoCall.setEnabled(true);
            }
        });

    }

}
