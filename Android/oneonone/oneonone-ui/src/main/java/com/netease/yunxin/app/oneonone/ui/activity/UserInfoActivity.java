// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.databinding.ActivityOneOnOneUserInfoBinding;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.UserInfoDetailModel;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.app.oneonone.ui.utils.AccountAmountHelper;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.NavUtils;
import com.netease.yunxin.app.oneonone.ui.utils.PhoneBindUtil;
import com.netease.yunxin.app.oneonone.ui.utils.UserInfoManager;
import com.netease.yunxin.app.oneonone.ui.viewmodel.UserInfoViewModel;
import retrofit2.Call;
import retrofit2.Callback;

public class UserInfoActivity extends BaseActivity {
  private ActivityOneOnOneUserInfoBinding binding;
  private static final String TAG = "UserInfoActivity";
  private int index;

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityOneOnOneUserInfoBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    parseBundle();
    initView();
    initEvent();
  }

  private void parseBundle() {
    index = getIntent().getIntExtra(AppParams.INDEX, 0);
  }

  private void initView() {
    ImmersionBar.with(this).statusBarView(binding.statusBarHolder).statusBarDarkFont(true).init();
    UserInfoAdapter userInfoAdapter = new UserInfoAdapter();
    binding.recycleView.setAdapter(userInfoAdapter);
    binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
    UserInfoViewModel viewModel = new ViewModelProvider(this).get(UserInfoViewModel.class);
    viewModel.fetchData(index);
    viewModel
        .getDetail()
        .observe(
            this,
            new Observer<UserInfoDetailModel>() {
              @Override
              public void onChanged(UserInfoDetailModel userInfoDetailModel) {
                userInfoAdapter.bindData(userInfoDetailModel);
              }
            });
  }

  private void initEvent() {
    binding.rlAudioCall.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            binding.rlAudioCall.setEnabled(false);
            binding.rlVideoCall.setEnabled(false);
            handleCallLogic(ChannelType.AUDIO.getValue());
          }
        });
    binding.rlVideoCall.setOnClickListener(
        new View.OnClickListener() {
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
    if (channelType == ChannelType.AUDIO.getValue()) {
      gotoCallPage(
          channelType,
          AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid())
              && OneOnOneUI.getInstance().isChineseEnv());
    } else {
      gotoCallPage(channelType, false);
    }
  }

  private void gotoCallPage(int channelType, boolean needPstnCall) {
    String phoneNumber = PhoneBindUtil.getLastBindPhoneNumber();
    HttpService.getInstance()
        .searchUserInfoWithPhoneNumber(
            phoneNumber,
            new Callback<ModelResponse<UserModel>>() {

              @Override
              public void onResponse(
                  @NonNull Call<ModelResponse<UserModel>> call,
                  @NonNull retrofit2.Response<ModelResponse<UserModel>> response) {
                if (response.isSuccessful()
                    && response.body() != null
                    && response.body().data != null) {
                  LogUtil.i(
                      TAG,
                      "phoneNumber:" + phoneNumber + ",imAccid:" + response.body().data.imAccid);
                  NavUtils.toCallPage(
                      UserInfoActivity.this,
                      response.body().data,
                      channelType,
                      needPstnCall,
                      CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
                } else {
                  LogUtil.e(TAG, "call failed errorMsg:" + response.body().code);
                  ToastUtils.showShort(R.string.phonenumber_error_tips);
                }
                binding.rlAudioCall.setEnabled(true);
                binding.rlVideoCall.setEnabled(true);
              }

              @Override
              public void onFailure(
                  @NonNull Call<ModelResponse<UserModel>> call, @NonNull Throwable t) {
                LogUtil.e(TAG, "call failed errorMsg:" + t.getMessage());
                ToastUtils.showShort(R.string.one_on_one_network_error);
                binding.rlAudioCall.setEnabled(true);
                binding.rlVideoCall.setEnabled(true);
              }
            });
  }
}
