// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.call;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.databinding.FragmentInVideoCallBinding;
import com.netease.yunxin.app.one2one.model.OtherUserInfo;
import com.netease.yunxin.app.one2one.ui.biz.beauty.BeautyManager;
import com.netease.yunxin.app.one2one.ui.biz.call.viewmodel.CallViewModel;
import com.netease.yunxin.app.one2one.ui.view.InTheVideoCallBottomBar;
import com.netease.yunxin.app.one2one.utils.AppGlobals;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.NECallback;
import com.netease.yunxin.app.one2one.utils.TimeUtil;
import com.netease.yunxin.app.one2one.utils.security.SecurityTipsModel;
import com.netease.yunxin.app.one2one.utils.security.SecurityType;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.ui.base.CallParam;

public class InTheVideoCallFragment extends Fragment {
  private static final String TAG = "InTheVideoCallFragment";
  private FragmentInVideoCallBinding binding;
  private CallViewModel viewModel;
  private CallActivity activity;
  private boolean muteLocal = false;
  private boolean muteRemote = false;
  /** 本端摄像头是否打开 */
  private boolean localCameraIsOpen = true;
  /** 远端摄像头打开是否打开 */
  private boolean remoteCameraIsOpen = true;
  /** 本端是否为小屏 */
  private boolean isSelfInSmallUi = true;

  private NERTCVideoCall rtcCall;
  private String otherUid;
  private SecurityTipsModel securityTipsModel;

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
    binding = FragmentInVideoCallBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(requireActivity()).get(CallViewModel.class);
    rtcCall = activity.getRtcCall();
    BeautyManager.getInstance().startBeauty();
    subscribeUi();
    initEvent();
    return binding.getRoot();
  }

  private void setupLocalView(NERtcVideoView videoView) {
    NERtcEx.getInstance().setupLocalVideoCanvas(null);
    NERtcEx.getInstance().enableLocalVideo(true);
    videoView.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
    NERtcEx.getInstance().setupLocalVideoCanvas(videoView);
  }

  private void subscribeUi() {
    binding.smallVideo.setZOrderMediaOverlay(true);
    binding.bigVideo.setZOrderMediaOverlay(false);
    viewModel.refresh(activity.getCallParams());
    LifecycleOwner viewLifecycleOwner = getViewLifecycleOwner();
    viewModel
        .getOtherInfo()
        .observe(
            viewLifecycleOwner,
            new Observer<OtherUserInfo>() {
              @Override
              public void onChanged(OtherUserInfo otherUserInfo) {
                otherUid = otherUserInfo.accId;
                rtcCall.setupRemoteView(binding.bigVideo, otherUid);
                setupLocalView(binding.smallVideo);
                binding.tvNickname.setText(otherUserInfo.nickname);
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
                InTheVideoCallFragment.this.securityTipsModel = securityTipsModel;
                handleSecurityTips(securityTipsModel);
                handleSecurityVideoMask(securityTipsModel);
              }
            });
    viewModel
        .getInTheCallDuration()
        .observe(
            viewLifecycleOwner,
            new Observer<Long>() {
              @Override
              public void onChanged(Long aLong) {
                binding.tvDuration.setText(TimeUtil.formatSecondTime(aLong));
              }
            });
    viewModel
        .getRemoteVideoMute()
        .observe(
            viewLifecycleOwner,
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                remoteCameraIsOpen = !aBoolean;
                handleUi();
              }
            });
    viewModel
        .getCallFinished()
        .observe(
            viewLifecycleOwner,
            new Observer<Boolean>() {
              @Override
              public void onChanged(Boolean aBoolean) {
                ToastUtils.showShort(R.string.other_end_call);
                if (!activity.isFinishing()) {
                  activity.finish();
                }
              }
            });
  }

  private void handleUi() {
    handleSmallVideoUi();
    handleBigVideoUi();
  }

  private void handleBigVideoUi() {
    LogUtil.i(
        TAG,
        "handleBigVideoUi localVideoIsSmall:"
            + isSelfInSmallUi
            + ",localCameraIsOpen:"
            + localCameraIsOpen
            + ",remoteCameraIsOpen:"
            + remoteCameraIsOpen);
    if (!isSelfInSmallUi) {
      //大屏是自己
      showBigVideoView(localCameraIsOpen, true);
    } else {
      //大屏是对方
      showBigVideoView(remoteCameraIsOpen, false);
    }
  }

  private void showBigVideoView(boolean show, boolean isSelf) {
    if (show) {
      binding.bigVideo.setVisibility(View.VISIBLE);
      binding.tvBigVideoDesc.setVisibility(View.GONE);
      binding.clRoot.setBackgroundResource(0);
    } else {
      binding.bigVideo.setVisibility(View.GONE);
      binding.tvBigVideoDesc.setVisibility(View.VISIBLE);
      binding.clRoot.setBackgroundResource(R.drawable.bg_video_call_page);
      if (isSelf) {
        binding.tvBigVideoDesc.setText(R.string.already_close_camera);
      } else {
        binding.tvBigVideoDesc.setText(R.string.other_already_close_camera);
      }
    }
  }

  private void handleSmallVideoUi() {
    LogUtil.i(
        TAG,
        "handleSmallVideoUi localVideoIsSmall:"
            + isSelfInSmallUi
            + ",localCameraIsOpen:"
            + localCameraIsOpen
            + ",remoteCameraIsOpen:"
            + remoteCameraIsOpen);
    if (isSelfInSmallUi) {
      //小屏是自己
      if (localCameraIsOpen) {
        // 摄像头打开
        binding.smallVideo.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setVisibility(View.GONE);
        binding.ivCameraState.setImageResource(R.drawable.icon_camera_open);
      } else {
        // 摄像头关闭
        binding.smallVideo.setVisibility(View.GONE);
        binding.tvSmallVideoDesc.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setText(R.string.already_close_camera);
        binding.ivCameraState.setImageResource(R.drawable.icon_camera_close);
      }
      binding.flLocalCamera.setVisibility(View.VISIBLE);
    } else {
      //小屏是对方
      if (remoteCameraIsOpen) {
        // 摄像头打开
        binding.smallVideo.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setVisibility(View.GONE);
      } else {
        // 摄像头关闭
        binding.smallVideo.setVisibility(View.GONE);
        binding.tvSmallVideoDesc.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setText(R.string.other_already_close_camera);
      }
      binding.flLocalCamera.setVisibility(View.GONE);
    }
  }

  private void handleSecurityTips(SecurityTipsModel securityTipsModel) {
    if (securityTipsModel.self == null && securityTipsModel.other == null) {
      binding.securityTips.hide();
    } else {
      if (securityTipsModel.self != null) {
        binding.securityTips.setText(securityTipsModel.self.tips);
      } else {
        binding.securityTips.setText(securityTipsModel.other.tips);
      }
      binding.securityTips.show(-1);
    }
  }

  private void handleSecurityVideoMask(SecurityTipsModel securityTipsModel) {
    if (securityTipsModel == null) {
      binding.smallVideoSecurityMask.setVisibility(View.GONE);
      binding.bigVideoSecurityMask.setVisibility(View.GONE);
      return;
    }
    if (securityTipsModel.self != null && securityTipsModel.self.type != SecurityType.AUDIO) {
      if (isSelfInSmallUi) {
        binding.smallVideoSecurityMask.setVisibility(View.VISIBLE);
      } else {
        binding.bigVideoSecurityMask.setVisibility(View.VISIBLE);
      }
    } else {
      if (isSelfInSmallUi) {
        binding.smallVideoSecurityMask.setVisibility(View.GONE);
      } else {
        binding.bigVideoSecurityMask.setVisibility(View.GONE);
      }
    }

    if (securityTipsModel.other != null && securityTipsModel.other.type != SecurityType.AUDIO) {
      if (isSelfInSmallUi) {
        binding.bigVideoSecurityMask.setVisibility(View.VISIBLE);
      } else {
        binding.smallVideoSecurityMask.setVisibility(View.VISIBLE);
      }
    } else {
      if (isSelfInSmallUi) {
        binding.bigVideoSecurityMask.setVisibility(View.GONE);
      } else {
        binding.smallVideoSecurityMask.setVisibility(View.GONE);
      }
    }
  }

  private void initEvent() {
    binding.flSmallVideo.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (TextUtils.isEmpty(otherUid)) {
              return;
            }
            isSelfInSmallUi = !isSelfInSmallUi;
            switchVideosCanvas(isSelfInSmallUi);
            handleUi();
            handleSecurityVideoMask(securityTipsModel);
          }
        });

    binding.flLocalCamera.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (TextUtils.isEmpty(otherUid)) {
              return;
            }
            localCameraIsOpen = !localCameraIsOpen;
            rtcCall.muteLocalVideo(!localCameraIsOpen);
            handleUi();
          }
        });

    binding.bottomBar.setOnItemClickListener(
        new InTheVideoCallBottomBar.OnItemClickListener() {
          @Override
          public void onMicroPhoneButtonClick() {
            handleMircoPhoneEvent();
          }

          @Override
          public void onAudioButtonClick() {
            handleMuteAudioEvent();
          }

          @Override
          public void onSwitchCameraButtonClick() {
            activity.getRtcCall().switchCamera();
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

  private void handleHangupEvent() {
    ToastUtils.showShort(R.string.end_video);
    activity.rtcHangup(
        new NECallback<Integer>() {
          @Override
          public void onSuccess(Integer integer) {}

          @Override
          public void onError(int code, String errorMsg) {}
        });
    if (!activity.isFinishing()) {
      activity.finish();
    }
  }

  private void handleMuteAudioEvent() {
    muteRemote = !muteRemote;
    CallParam callParam = activity.getCallParams();
    if (callParam.isCalled()) {
      activity.getRtcCall().setAudioMute(muteRemote, callParam.getCallerAccId());
    } else {
      activity.getRtcCall().setAudioMute(muteRemote, callParam.getCalledAccIdList().get(0));
    }
    if (muteRemote) {
      binding.bottomBar.getViewBinding().ivAudio.setImageResource(R.drawable.icon_audio_mute);
    } else {
      binding.bottomBar.getViewBinding().ivAudio.setImageResource(R.drawable.icon_audio);
    }
  }

  private void handleMircoPhoneEvent() {
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

  private void switchVideosCanvas(boolean isSelfInSmallUi) {
    if (isSelfInSmallUi) {
      setupLocalView(binding.smallVideo);
      rtcCall.setupRemoteView(binding.bigVideo, otherUid);
    } else {
      setupLocalView(binding.bigVideo);
      rtcCall.setupRemoteView(binding.smallVideo, otherUid);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    BeautyManager.getInstance().stopBeauty();
    binding = null;
  }
}
