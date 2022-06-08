/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.databinding.ActivityBindOtherPhoneBinding;
import com.netease.yunxin.app.one2one.http.HttpService;
import com.netease.yunxin.app.one2one.model.UserModel;
import com.netease.yunxin.app.one2one.ui.view.StatusBarConfig;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.PhoneBindUtil;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;

import io.reactivex.observers.ResourceSingleObserver;

/**
 * 绑定对方账号
 */
public class BindOtherPhoneActivity extends BaseActivity {
    private ActivityBindOtherPhoneBinding binding;
    private String currentPhoneNumber;

    private final static String TAG = "BindOtherPhoneActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBindOtherPhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initEvent();
    }

    @SuppressLint("SetTextI18n")
    private void initEvent() {
        if (TextUtils.isEmpty(PhoneBindUtil.getLastBindPhoneNumber())) {
            binding.tvPhoneNumber.setText(R.string.binded_phone_number_empty);
        } else {
            binding.tvPhoneNumber.setText(getString(R.string.other_phone_number) + PhoneBindUtil.getLastBindPhoneNumber());
        }
        binding.tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSaveLogic();
            }
        });
        binding.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                currentPhoneNumber = editable.toString();
            }
        });
    }

    private void handleSaveLogic() {
        if (!RegexUtils.isMobileSimple(currentPhoneNumber)) {
            ToastUtils.showShort(R.string.please_input_correct_phone_number);
            return;
        }
        if (UserInfoManager.getSelfUserInfo().getUser().equals(currentPhoneNumber)){
            ToastUtils.showShort(R.string.bind_mine_phone_number_error_tips);
            return;
        }
        if (!NetworkUtils.isConnected()){
            ToastUtils.showShort(R.string.network_error);
            return;
        }
        HttpService.searchUserInfoWithPhoneNumber(currentPhoneNumber).subscribe(new ResourceSingleObserver<BaseResponse<UserModel>>() {
            @Override
            public void onSuccess(BaseResponse<UserModel> userModelBaseResponse) {
                if (userModelBaseResponse.isSuccessful()&&userModelBaseResponse.data!=null) {
                    PhoneBindUtil.setLastBindPhoneNumber(currentPhoneNumber);
                    LogUtil.i(TAG, "bind success,phoneNumber is:" + currentPhoneNumber);
                    ToastUtils.showShort(R.string.save_success);
                    finish();
                } else {
                    ToastUtils.showLong(R.string.bind_other_phone_failed_tips);
                    LogUtil.e(TAG, "bind failed,msg:" + userModelBaseResponse.msg);
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showShort(R.string.network_error);
                LogUtil.e(TAG, "bind failed,e:" + e);
            }
        });
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
