// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.databinding.OneOnOneRvItemHomeBinding;
import com.netease.yunxin.app.oneonone.ui.dialog.SelectCallTypeDialog;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.HomeItemModel;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.app.oneonone.ui.utils.AccountAmountHelper;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.DialogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.DisplayUtils;
import com.netease.yunxin.app.oneonone.ui.utils.NavUtils;
import com.netease.yunxin.app.oneonone.ui.utils.UserInfoManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.dialog.CommonAlertDialog;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.service.XKitServiceManager;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeAdapter extends RecyclerView.Adapter {
  private static final String TAG = "HomeAdapter";
  private List<HomeItemModel> list = new ArrayList<>();
  private AppCompatActivity activity;
  private static final String ONLINE = "online";

  public HomeAdapter(AppCompatActivity activity) {
    this.activity = activity;
  }

  public void bindData(List<HomeItemModel> list) {
    this.list.clear();
    this.list.addAll(list);
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    OneOnOneRvItemHomeBinding binding =
        OneOnOneRvItemHomeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
    if (list == null || list.isEmpty()) {
      return 0;
    }
    return list.size();
  }

  public static class HomeHolder extends RecyclerView.ViewHolder {
    private OneOnOneRvItemHomeBinding binding;
    private AppCompatActivity activity;

    public HomeHolder(AppCompatActivity activity, @NonNull OneOnOneRvItemHomeBinding binding) {
      super(binding.getRoot());
      this.activity = activity;
      this.binding = binding;
    }

    @SuppressLint("SetTextI18n")
    public void bindData(HomeItemModel homeItemModel, int position) {
      Context context = AppGlobals.getApplication();
      ImageLoader.with(context)
          .roundedCorner(homeItemModel.icon, (int) DisplayUtils.dp2px(4), binding.iv);
      binding.tv.setText(homeItemModel.userName);
      binding
          .getRoot()
          .setOnClickListener(
              view -> {
                if (ClickUtils.isFastClick()) {
                  return;
                }

                SelectCallTypeDialog dialog = new SelectCallTypeDialog(activity);
                dialog.setDialogCallback(
                    new SelectCallTypeDialog.SelectCallTypeCallback() {
                      @Override
                      public void onVideoCall(Dialog dialog) {
                        if (!NetworkUtils.isConnected()) {
                          ToastUtils.showShort(
                              context.getString(R.string.one_on_one_network_error));
                          return;
                        }
                        handleCall(ChannelType.VIDEO.getValue(), homeItemModel);
                        dialog.dismiss();
                      }

                      @Override
                      public void onAudioCall(Dialog dialog) {
                        if (!NetworkUtils.isConnected()) {
                          ToastUtils.showShort(
                              context.getString(R.string.one_on_one_network_error));
                          return;
                        }
                        handleCall(ChannelType.AUDIO.getValue(), homeItemModel);
                        dialog.dismiss();
                      }
                    });
                dialog.show();
              });
      //            binding.onlineState.setVisibility(View.VISIBLE);
      //            binding.viewOnlineState.setVisibility(View.VISIBLE);
    }

    private void handleCall(int channelType, HomeItemModel homeItemModel) {

      Object result =
          XKitServiceManager.Companion.getInstance()
              .callService("VoiceRoomXKitService", "getCurrentRoomInfo", null);
      if (result instanceof Boolean) {
        boolean isInVoiceRoom = (boolean) result;
        ALog.d(TAG, "isInVoiceRoom:" + isInVoiceRoom);
        if (isInVoiceRoom) {
          showTipsDialog(activity.getString(R.string.one_on_one_other_you_are_in_the_chatroom));
          return;
        }
      }
      HttpService.getInstance()
          .getUserState(
              homeItemModel.mobile,
              new Callback<ModelResponse<String>>() {
                @Override
                public void onResponse(
                    Call<ModelResponse<String>> call, Response<ModelResponse<String>> response) {
                  if (response.body() != null && response.body().code == 200) {
                    String onlineState = response.body().data;
                    if (ONLINE.equals(onlineState)) {
                      if (channelType == ChannelType.AUDIO.getValue()) {
                        gotoCallPage(
                            ChannelType.AUDIO.getValue(),
                            AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid())
                                && OneOnOneUI.getInstance().isChineseEnv(),
                            homeItemModel,
                            CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
                      } else {
                        gotoCallPage(
                            channelType,
                            false,
                            homeItemModel,
                            CallConfig.CALL_PSTN_WAIT_MILLISECONDS);
                      }
                    } else {
                      if (channelType == ChannelType.AUDIO.getValue()) {
                        // 对方不在线，直接走PSTN呼叫
                        gotoCallPage(
                            ChannelType.AUDIO.getValue(),
                            AccountAmountHelper.allowPstnCall(UserInfoManager.getSelfImAccid())
                                && OneOnOneUI.getInstance().isChineseEnv(),
                            homeItemModel,
                            0);
                      } else {
                        DialogUtil.showConfirmDialog(
                            activity,
                            activity.getString(
                                com.netease
                                    .yunxin
                                    .app
                                    .oneonone
                                    .ui
                                    .R
                                    .string
                                    .one_on_one_other_is_not_online));
                      }
                    }
                  }
                }

                @Override
                public void onFailure(Call<ModelResponse<String>> call, Throwable t) {
                  ALog.e(TAG, "getUserState failed,t:" + t);
                }
              });
    }

    private void gotoCallPage(
        int channelType,
        boolean needPstnCall,
        HomeItemModel homeItemModel,
        long callPstnWaitMilliseconds) {
      UserModel userModel = new UserModel();
      userModel.imAccid = homeItemModel.accountId;
      userModel.nickname = homeItemModel.userName;
      userModel.avatar = homeItemModel.icon;
      userModel.mobile = homeItemModel.mobile;
      NavUtils.toCallPage(activity, userModel, channelType, needPstnCall, callPstnWaitMilliseconds);
    }

    private void showTipsDialog(String content) {
      CommonAlertDialog commonDialog = new CommonAlertDialog();
      commonDialog
          .setTitleStr(content)
          .setPositiveStr(activity.getString(R.string.one_on_one_confirm))
          .setConfirmListener(() -> {})
          .show(activity.getSupportFragmentManager());
    }
  }
}
