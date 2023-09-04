// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.databinding.FragmentCallBinding;
import com.netease.yunxin.app.oneonone.ui.model.OtherUserInfo;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.DisplayUtils;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.utils.NERTCCallStateManager;
import com.netease.yunxin.app.oneonone.ui.utils.RtcUtil;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CallViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import org.json.JSONException;
import org.json.JSONObject;

public class CallFragment extends Fragment {
  private static final String TAG = "CallFragment";
  private FragmentCallBinding binding;
  private CallActivity activity;
  private CallViewModel viewModel;
  private CallParam callParams;
  private boolean callFinished = true;
  private String calledMobile;
  private String callerUserName;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (CallActivity) context;
    OnBackPressedCallback callback =
        new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.end_call))
                .setMessage(activity.getString(R.string.sure_end_call))
                .setPositiveButton(
                    activity.getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                        if (!callFinished) {
                          ToastX.showShortToast(R.string.call_out_failed);
                          finishActivity();
                          return;
                        }
                        handleHangupEvent();
                      }
                    })
                .setNegativeButton(
                    activity.getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                      }
                    })
                .show();
          }
        };
    activity
        .getOnBackPressedDispatcher()
        .addCallback(
            this, // LifecycleOwner
            callback);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentCallBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    callParams = activity.getCallParams();
    Bundle bundle = getArguments();
    if (bundle != null && bundle.getBoolean(AppParams.AUTO_CALL, false) && !callParams.isCalled()) {
      handleCall();
    }
    if (callParams.isCalled()) {
      // 被叫逻辑,被叫扬声器响铃,系统默认是扬声器播放
      playRing(AVChatSoundPlayer.RingerTypeEnum.RING);
      RtcUtil.setSpeakerphoneOn(requireActivity(), true);
    }
    subscribeUi();
    initEvent();
    return binding.getRoot();
  }

  public void handleCall() {
    // 主叫逻辑
    if (callParams.getCallExtraInfo() == null) {
      return;
    }
    JSONObject callParamExtraInfo = null;
    try {
      callParamExtraInfo = new JSONObject(callParams.getCallExtraInfo());
    } catch (JSONException e) {
      ALog.e(TAG, "handleCall,json parse callParamExtraInfo error:" + e.getMessage());
    }
    if (!callParams.isCalled()) {
      callFinished = false;
      if (callParamExtraInfo != null) {
        try {
          calledMobile = callParamExtraInfo.getString(AppParams.CALLED_USER_MOBILE);
        } catch (JSONException e) {
          ALog.e(TAG, "handleCall,json parse calledMobile error:" + e.getMessage());
        }
        try {
          callerUserName = callParamExtraInfo.getString(AppParams.CALLER_USER_NAME);
        } catch (JSONException e) {
          ALog.e(TAG, "handleCall,json parse callerUserName error:" + e.getMessage());
        }
      }
      if (activity.isVirtualCall()) {
        NERTCCallStateManager.setCallOutState();
        // 主叫逻辑,虚拟人呼叫，音频通话设置为听筒播放、视频通话设置为扬声器播放
        RtcUtil.setSpeakerphoneOn(
            requireActivity(), callParams.getChannelType() != ChannelType.AUDIO.getValue());
        AVChatSoundPlayer.INSTANCE.play(activity, AVChatSoundPlayer.RingerTypeEnum.RING);
      } else {
        activity.rtcCall(
            new NECallback<ChannelFullInfo>() {
              @Override
              public void onSuccess(ChannelFullInfo channelFullInfo) {
                callFinished = true;
                AVChatSoundPlayer.INSTANCE.play(activity, AVChatSoundPlayer.RingerTypeEnum.RING);
                // 主叫逻辑,音频通话设置为听筒播放、视频通话设置为扬声器播放
                NERtcEx.getInstance()
                    .setSpeakerphoneOn(callParams.getChannelType() != ChannelType.AUDIO.getValue());
              }

              @Override
              public void onError(int code, String errorMsg) {
                ToastX.showShortToast(R.string.call_failed);
              }
            });
      }
      LogUtil.i(TAG, "handleCall->rtcCall");
    }
  }

  private void subscribeUi() {
    viewModel.refresh(callParams);
    handleOtherInfoUi();
    handleRingEvent();
  }

  private void handleRingEvent() {
    viewModel
        .getPlayRing()
        .observe(
            getViewLifecycleOwner(),
            new Observer<AVChatSoundPlayer.RingerTypeEnum>() {
              @Override
              public void onChanged(AVChatSoundPlayer.RingerTypeEnum ringerTypeEnum) {
                playRing(ringerTypeEnum);
              }
            });
  }

  private void handleOtherInfoUi() {
    viewModel
        .getOtherInfo()
        .observe(
            getViewLifecycleOwner(),
            new Observer<OtherUserInfo>() {
              @Override
              public void onChanged(OtherUserInfo otherUserInfo) {
                if (otherUserInfo == null) {
                  return;
                }
                if (otherUserInfo.callType == ChannelType.VIDEO.getValue()) {
                  binding.clRoot.setBackgroundResource(R.drawable.bg_video_call_page);
                  binding.ivInvitedAccept.setBackgroundResource(
                      R.drawable.selector_img_video_accept);
                } else {
                  binding.clRoot.setBackgroundResource(R.drawable.bg_audio_call_page);
                  binding.ivInvitedAccept.setBackgroundResource(
                      R.drawable.selector_img_audio_accept);
                }
                binding.tvNick.setText(otherUserInfo.nickname);
                ImageLoader.with(AppGlobals.getApplication())
                    .circleLoad(otherUserInfo.avatar, binding.ivAvatar);
                binding.tvTitle.setText(otherUserInfo.title);
                binding.tvSubtitle.setText(otherUserInfo.subtitle);
                if (otherUserInfo.isCalled) {
                  binding.llInvite.setVisibility(View.GONE);
                  binding.rlInvited.setVisibility(View.VISIBLE);
                  binding.tvSubtitle.setTextColor(Color.parseColor("#8CFFFFFF"));
                  binding.tvSubtitle.setPadding(0, 0, 0, 0);
                } else {
                  binding.llInvite.setVisibility(View.VISIBLE);
                  binding.rlInvited.setVisibility(View.GONE);
                  binding.tvSubtitle.setTextColor(Color.WHITE);
                  binding.tvSubtitle.setPadding(0, (int) DisplayUtils.dp2px(24), 0, 0);
                }
              }
            });
  }

  private void initEvent() {

    binding.ivInviteCancel.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            handleHangupEvent();
          }
        });

    binding.ivInvitedAccept.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            stopRing();
            RtcUtil.resetAudioManagerMode(requireContext());
            handleInvitedAcceptEvent();
          }
        });

    binding.ivInvitedReject.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            handleHangupEvent();
          }
        });

    binding.clRoot.setOnClickListener(v -> {});
  }

  private void handleHangupEvent() {
    activity.rtcHangup(null);
    if (callParams.isCalled()) {
      binding.ivInvitedAccept.setEnabled(false);
      binding.ivInvitedReject.setEnabled(false);
    } else {
      binding.ivInviteCancel.setEnabled(false);
    }
    finishActivity();
  }

  private void handleInvitedAcceptEvent() {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(getString(R.string.one_on_one_network_error));
      return;
    }
    boolean microPhoneGranted =
        PermissionUtils.hasPermissions(getContext(), Manifest.permission.RECORD_AUDIO);
    boolean cameraGranted =
        PermissionUtils.hasPermissions(getContext(), Manifest.permission.CAMERA);
    if (callParams.getChannelType() == ChannelType.AUDIO.getValue() && !microPhoneGranted) {
      ToastX.showShortToast(getString(R.string.permission_microphone_missing_tips));
      ALog.e(TAG, "Unable to access the microphone. Enable microphone access and try again");
      return;
    } else if (callParams.getChannelType() == ChannelType.VIDEO.getValue()) {
      if (!microPhoneGranted && !cameraGranted) {
        ToastX.showShortToast(getString(R.string.permission_microphone_and_camera_missing_tips));
        ALog.e(
            TAG,
            "Unable to access the microphone and camera. Enable microphone and camera access and try again");
        return;
      } else if (!microPhoneGranted) {
        ToastX.showShortToast(getString(R.string.permission_microphone_missing_tips));
        ALog.e(TAG, "Unable to access the microphone. Enable microphone access and try again");
        return;
      } else if (!cameraGranted) {
        ToastX.showShortToast(getString(R.string.permission_camera_missing_tips));
        ALog.e(TAG, "Unable to access the camera. Enable camera access and try again");
        return;
      }
    }
    activity.rtcAccept();

    binding.ivInvitedAccept.setEnabled(false);
    binding.ivInvitedReject.setEnabled(false);
  }

  private void releaseAndFinish(boolean finishCall) {
    LogUtil.i(
        TAG, "releaseAndFinish,finishCall:" + finishCall + ",isCalled:" + callParams.isCalled());
    AVChatSoundPlayer.INSTANCE.stop(AVChatSoundPlayer.RingerTypeEnum.RING, activity);
    AVChatSoundPlayer.INSTANCE.stop(AVChatSoundPlayer.RingerTypeEnum.CONNECTING, activity);
    if (finishCall) {
      if (callParams.isCalled()) {
        activity.rtcHangup(
            new NECallback<Integer>() {
              @Override
              public void onSuccess(Integer integer) {
                finishActivity();
              }

              @Override
              public void onError(int code, String errorMsg) {
                finishActivity();
              }
            });
      } else {
        pstnHangup();
      }
    }
  }

  private void playRing(AVChatSoundPlayer.RingerTypeEnum ringerTypeEnum) {
    AVChatSoundPlayer.INSTANCE.play(activity, ringerTypeEnum);
  }

  private void stopRing() {
    AVChatSoundPlayer.INSTANCE.stop(activity);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void pstnHangup() {
    finishActivity();
  }

  private void finishActivity() {
    if (!activity.isFinishing()) {
      activity.finish();
    }
  }
}
