// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.databinding.FragmentInAudioCallBinding;
import com.netease.yunxin.app.oneonone.ui.model.OtherUserInfo;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.utils.TimeUtil;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityTipsModel;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityType;
import com.netease.yunxin.app.oneonone.ui.view.InTheAudioCallBottomBar;
import com.netease.yunxin.kit.common.image.ImageLoader;

public class InTheAudioCallFragment extends InTheBaseCallFragment {
  private static final String TAG = "InTheAudioCallFragment";
  private FragmentInAudioCallBinding binding;
  private CallActivity activity;
  private boolean muteLocal = false;
  private long otherRtcUid;

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

  @Override
  protected View getRoot(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FragmentInAudioCallBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  protected void subscribeUi() {
    super.subscribeUi();
    viewModel.refresh(activity.getCallParams());
    LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
    viewModel
        .getOtherInfo()
        .observe(
            viewLifecycleOwner,
            new Observer<OtherUserInfo>() {
              @Override
              public void onChanged(OtherUserInfo otherUserInfo) {
                binding.tvNick.setText(otherUserInfo.nickname);
                ImageLoader.with(AppGlobals.getApplication())
                    .circleLoad(otherUserInfo.avatar, binding.ivAvatar);
              }
            });

    viewModel
        .getSecurityTipsModel()
        .observe(
            viewLifecycleOwner,
            new Observer<SecurityTipsModel>() {
              @Override
              public void onChanged(SecurityTipsModel securityTipsModel) {
                handleSecurityTips(securityTipsModel);
              }
            });
    viewModel
        .getInTheCallDuration()
        .observe(
            viewLifecycleOwner,
            new Observer<Long>() {
              @Override
              public void onChanged(Long aLong) {
                binding.tvSubtitle.setText(TimeUtil.formatSecondTime(aLong));
              }
            });
    viewModel
        .getOtherRtcUid()
        .observe(
            viewLifecycleOwner,
            new Observer<Long>() {
              @Override
              public void onChanged(Long aLong) {
                otherRtcUid = aLong;
              }
            });

    if (virtualCallViewModel != null) {
      virtualCallViewModel
          .getInTheCallDuration()
          .observe(
              viewLifecycleOwner,
              aLong -> binding.tvSubtitle.setText(TimeUtil.formatSecondTime(aLong)));
    }
  }

  private void handleSecurityTips(SecurityTipsModel securityTipsModel) {
    if (securityTipsModel.self == null && securityTipsModel.other == null) {
      binding.securityTips.hide();
    } else {
      if (securityTipsModel.self != null && securityTipsModel.self.type == SecurityType.AUDIO) {
        binding.securityTips.setText(securityTipsModel.self.tips);
      } else if (securityTipsModel.other.type == SecurityType.AUDIO) {
        binding.securityTips.setText(securityTipsModel.other.tips);
      }
      binding.securityTips.show(-1);
    }
  }

  @Override
  protected void initEvent() {
    super.initEvent();
    binding.bottomBar.setOnItemClickListener(
        new InTheAudioCallBottomBar.OnItemClickListener() {
          @Override
          public void onMicroPhoneButtonClick() {
            handleMicroPhoneEvent();
          }

          @Override
          public void onAudioButtonClick() {
            handleAudioEvent();
          }

          @Override
          public void onHangupButtonClick() {
            handleHangupEvent();
          }
        });

    binding.sendGift.setOnClickListener(
        v -> {
          showGiftDialog();
        });
  }

  private void handleAudioEvent() {
    boolean lastSpeakerOn = NERtcEx.getInstance().isSpeakerphoneOn();
    boolean currentSpeakOn = !lastSpeakerOn;
    NERtcEx.getInstance().setSpeakerphoneOn(currentSpeakOn);
    binding
        .bottomBar
        .getViewBinding()
        .ivAudio
        .setImageResource(currentSpeakOn ? R.drawable.icon_audio : R.drawable.icon_audio_mute);
  }

  private void handleMicroPhoneEvent() {
    muteLocal = !muteLocal;
    activity.getRtcCall().muteLocalAudio(muteLocal);
    if (muteLocal) {
      binding
          .bottomBar
          .getViewBinding()
          .ivMicrophone
          .setImageResource(R.drawable.icon_microphone_mute);
    } else {
      binding.bottomBar.getViewBinding().ivMicrophone.setImageResource(R.drawable.icon_microphone);
    }
  }

  private void handleHangupEvent() {
    activity.rtcHangup(
        new NECallback<Integer>() {
          @Override
          public void onSuccess(Integer integer) {}

          @Override
          public void onError(int code, String errorMsg) {}
        });
    finishActivity();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  private void finishActivity() {
    if (!activity.isFinishing()) {
      activity.finish();
    }
  }
}
