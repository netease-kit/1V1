// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.databinding.FragmentInAudioCallBinding;
import com.netease.yunxin.app.oneonone.ui.model.OtherUserInfo;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.utils.TimeUtil;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityTipsModel;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityType;
import com.netease.yunxin.app.oneonone.ui.view.InTheAudioCallBottomBar;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CallViewModel;
import com.netease.yunxin.app.oneonone.ui.viewmodel.PstnCallViewModel;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.CallParam;
import org.json.JSONException;
import org.json.JSONObject;

public class InTheAudioCallFragment extends Fragment {
  private static final String TAG = "InTheAudioCallFragment";
  private FragmentInAudioCallBinding binding;
  private CallActivity activity;
  private boolean muteLocal = false;
  private boolean muteRemote = false;
  private CallViewModel viewModel;
  private PstnCallViewModel pstnCallViewModel;
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

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentInAudioCallBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    pstnCallViewModel = new ViewModelProvider(requireActivity()).get(PstnCallViewModel.class);
    subscribeUi();
    initEvent();
    return binding.getRoot();
  }

  private void subscribeUi() {
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

    pstnCallViewModel
        .getPstnToastData()
        .observe(
            viewLifecycleOwner,
            new Observer<String>() {
              @Override
              public void onChanged(String s) {
                ToastUtils.showShort(s);
              }
            });
    pstnCallViewModel
        .getReleaseAndFinish()
        .observe(
            viewLifecycleOwner,
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                finishActivity();
              }
            });

    pstnCallViewModel
        .getOtherRtcUid()
        .observe(
            viewLifecycleOwner,
            new Observer<Long>() {
              @Override
              public void onChanged(Long aLong) {
                otherRtcUid = aLong;
              }
            });

    pstnCallViewModel
        .getInTheCallDuration()
        .observe(
            viewLifecycleOwner,
            new Observer<Long>() {
              @Override
              public void onChanged(Long aLong) {
                binding.tvSubtitle.setText(TimeUtil.formatSecondTime(aLong));
              }
            });
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

  private void initEvent() {
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

    binding.ivEar.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ToastUtils.showShort(R.string.earphone_tips);
          }
        });

    binding.ivGift.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ToastUtils.showShort(R.string.gift_tips);
          }
        });
  }

  private void handleAudioEvent() {
    if (!NetworkUtils.isConnected()) {
      ToastUtils.showShort(getString(R.string.one_on_one_network_error));
      return;
    }
    muteRemote = !muteRemote;
    subscribeRemoteAudioStream(otherRtcUid, !muteRemote);
    if (muteRemote) {
      binding.bottomBar.getViewBinding().ivAudio.setImageResource(R.drawable.icon_audio_mute);
    } else {
      binding.bottomBar.getViewBinding().ivAudio.setImageResource(R.drawable.icon_audio);
    }
  }

  private void handleMicroPhoneEvent() {
    if (!NetworkUtils.isConnected()) {
      ToastUtils.showShort(getString(R.string.one_on_one_network_error));
      return;
    }
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
    CallParam callParams = activity.getCallParams();
    if (callParams.isCalled()) {
      activity.rtcHangup(
          new NECallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}

            @Override
            public void onError(int code, String errorMsg) {}
          });
    } else {
      try {
        JSONObject jsonObject = new JSONObject(callParams.getCallExtraInfo());
        if (jsonObject.getBoolean(AppParams.NEED_PSTN_CALL)) {
          PstnFunctionMgr.hangup();

        } else {
          activity.rtcHangup(
              new NECallback<Integer>() {
                @Override
                public void onSuccess(Integer integer) {}

                @Override
                public void onError(int code, String errorMsg) {}
              });
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
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

  public void subscribeRemoteAudioStream(long rtcUid, boolean subscribe) {
    NERtcEx.getInstance().adjustUserPlaybackSignalVolume(rtcUid, subscribe ? 100 : 0);
  }
}
