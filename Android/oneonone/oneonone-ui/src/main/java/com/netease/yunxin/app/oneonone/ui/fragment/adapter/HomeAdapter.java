// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.databinding.OneOnOneRvItemHomeBinding;
import com.netease.yunxin.app.oneonone.ui.dialog.ContactUserDialog;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.HomeItemModel;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUtil;
import com.netease.yunxin.app.oneonone.ui.utils.DisplayUtils;
import com.netease.yunxin.app.oneonone.ui.utils.NavUtils;
import com.netease.yunxin.app.oneonone.ui.utils.OneOnOneUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.dialog.CommonAlertDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.entertainment.common.utils.ClickUtils;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
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

                ContactUserDialog dialog = new ContactUserDialog();
                dialog.setDialogCallback(
                    new ContactUserDialog.SelectCallTypeCallback() {
                      @Override
                      public void onAccost(ContactUserDialog dialog) {
                        if (!NetworkUtils.isConnected()) {
                          ToastX.showShortToast(context.getString(R.string.voiceroom_net_error));
                          return;
                        }
                        ChatUtil.sendTextMessage(
                            homeItemModel.userUuid,
                            SessionTypeEnum.P2P,
                            context.getString(R.string.one_on_one_accost_text),
                            false,
                            new FetchCallback<Void>() {
                              @Override
                              public void onSuccess(@Nullable Void param) {
                                ToastX.showShortToast(
                                    context.getString(R.string.one_on_one_accost_success));
                              }

                              @Override
                              public void onFailed(int code) {
                                ALog.e(TAG, "sendTextMessage failed,code:" + code);
                              }

                              @Override
                              public void onException(@Nullable Throwable exception) {
                                ALog.e(TAG, "sendTextMessage failed,exception:" + exception);
                              }
                            });
                        dialog.dismiss();
                      }

                      @Override
                      public void onPrivateLetter(ContactUserDialog dialog) {
                        //跳转到单聊界面
                        UserInfo userInfo =
                            new UserInfo(
                                homeItemModel.userUuid, homeItemModel.userName, homeItemModel.icon);
                        userInfo.setMobile(homeItemModel.mobile);
                        NavUtils.toP2pPage(context, userInfo, null);
                        dialog.dismiss();
                      }

                      @Override
                      public void onVideoCall(ContactUserDialog dialog) {
                        if (!NetworkUtils.isConnected()) {
                          ToastX.showShortToast(
                              context.getString(R.string.one_on_one_network_error));
                          return;
                        }
                        handleCall(ChannelType.VIDEO.getValue(), homeItemModel);
                        dialog.dismiss();
                      }

                      @Override
                      public void onAudioCall(ContactUserDialog dialog) {
                        if (!NetworkUtils.isConnected()) {
                          ToastX.showShortToast(
                              context.getString(R.string.one_on_one_network_error));
                          return;
                        }
                        handleCall(ChannelType.AUDIO.getValue(), homeItemModel);
                        dialog.dismiss();
                      }
                    });
                dialog.show(activity.getSupportFragmentManager(), ContactUserDialog.TAG);
              });
      //            binding.onlineState.setVisibility(View.VISIBLE);
      //            binding.viewOnlineState.setVisibility(View.VISIBLE);
    }

    private void handleCall(int channelType, HomeItemModel homeItemModel) {

      if (OneOnOneUtils.isInVoiceRoom()) {
        showTipsDialog(activity.getString(R.string.one_on_one_other_you_are_in_the_chatroom));
        return;
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
                      gotoCallPage(channelType, homeItemModel);
                    } else {
                      DialogUtil.showAlertDialog(
                          activity, activity.getString(R.string.one_on_one_other_is_not_online));
                    }
                  }
                }

                @Override
                public void onFailure(Call<ModelResponse<String>> call, Throwable t) {
                  ALog.e(TAG, "getUserState failed,t:" + t);
                }
              });
    }

    private void gotoCallPage(int channelType, HomeItemModel homeItemModel) {
      UserModel userModel = new UserModel();
      userModel.imAccid = homeItemModel.userUuid;
      userModel.nickname = homeItemModel.userName;
      userModel.avatar = homeItemModel.icon;
      userModel.mobile = homeItemModel.mobile;
      if (CallConfig.enableVirtualCall) {
        userModel.callType = homeItemModel.callType;
        userModel.audioUrl = homeItemModel.audioUrl;
        userModel.videoUrl = homeItemModel.videoUrl;
      }
      NavUtils.toCallPage(activity, userModel, channelType);
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
