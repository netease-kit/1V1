// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.video.NERtcVideoFrame;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;
import com.netease.neliveplayer.sdk.NELivePlayer;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.databinding.FragmentInVideoCallBinding;
import com.netease.yunxin.app.oneonone.ui.model.OtherUserInfo;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.utils.TimeUtil;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityTipsModel;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityType;
import com.netease.yunxin.app.oneonone.ui.view.InTheVideoCallBottomBar;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.common.image.ImageLoader;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackExTemp;
import com.netease.yunxin.nertc.ui.p2p.CallUIOperationsMgr;

public class InTheVideoCallFragment extends InTheBaseCallFragment implements SensorEventListener {
  private static final String TAG = "InTheVideoCallFragment";
  private FragmentInVideoCallBinding binding;
  private CallActivity activity;
  private NECallEngine rtcCall;
  private SecurityTipsModel securityTipsModel;
  private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
  private boolean isFirstInit = true;
  private int mSkipFrame = 5;
  private FURenderer mFURendererManager;
  private FaceUnityDataFactory mFaceUnityDataFactory;
  private SensorManager mSensorManager;

  private void refreshUIByFloatWindowEvent() {
    binding
        .bottomBar
        .getViewBinding()
        .ivMicrophone
        .setImageResource(
            viewModel.isMuteLocalAudio()
                ? R.drawable.icon_microphone_mute
                : R.drawable.icon_microphone);
    binding
        .bottomBar
        .getViewBinding()
        .ivAudio
        .setImageResource(
            !viewModel.isSpeakerOn() ? R.drawable.icon_audio_mute : R.drawable.icon_audio);
    binding
        .bottomBar
        .getViewBinding()
        .ivOpenOrCloseCamera
        .setImageResource(
            !viewModel.isLocalCameraIsOpen()
                ? R.drawable.icon_camera_close
                : R.drawable.icon_camera_open);
    handleInTheVideoCallUI();
    switchVideosCanvas(viewModel.isSelfInSmallUi());
    handleUi();
  }

  private final FURendererListener mFURendererListener =
      new FURendererListener() {
        @Override
        public void onPrepare() {
          mFaceUnityDataFactory.bindCurrentRenderer();
        }

        @Override
        public void onTrackStatusChanged(FUAIProcessorEnum type, int status) {}

        @Override
        public void onFpsChanged(double fps, double callTime) {}

        @Override
        public void onRelease() {}
      };

  private void handleVideoSizeChanged(int videoWidth, int videoHeight) {
    if (videoWidth > 0 && videoHeight > 0) {
      // 计算缩放比例
      float scaleX = (float) binding.textureView.getWidth() / videoWidth;
      float scaleY = (float) binding.textureView.getHeight() / videoHeight;
      float scale;
      if (videoWidth > videoHeight) {
        scale = Math.min(scaleX, scaleY);
      } else {
        scale = Math.max(scaleX, scaleY);
      }

      // 缩放后的视频尺寸
      int scaledWidth = Math.round(videoWidth * scale);
      int scaledHeight = Math.round(videoHeight * scale);
      // 更新TextureView的布局参数
      RelativeLayout.LayoutParams layoutParams =
          (RelativeLayout.LayoutParams) binding.textureView.getLayoutParams();
      layoutParams.width = scaledWidth;
      layoutParams.height = scaledHeight;
      binding.textureView.setLayoutParams(layoutParams);
    }
  }

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
    rtcCall = activity.getRtcCall();
    setupFaceUnity();
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  public void handleInTheVideoCallUI() {
    if (activity.isVirtualCall() && virtualCallViewModel != null) {
      binding.textureViewContainer.setVisibility(View.VISIBLE);
      binding.bigVideo.setVisibility(View.GONE);
      NELivePlayer player = virtualCallViewModel.getPlayer();
      if (virtualCallViewModel.isVideoType()) {
        if (binding.textureView.isAvailable()) {
          Surface surface = new Surface(binding.textureView.getSurfaceTexture());
          player.setSurface(surface);
        } else {
          binding.textureView.setSurfaceTextureListener(
              new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                    @NonNull SurfaceTexture surface, int width, int height) {
                  player.setSurface(new Surface(surface));
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                    @NonNull SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                  return false;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
              });
        }
      }
      try {
        NERtcEx.getInstance().release();
        NERtcEx.getInstance()
            .init(
                requireContext(),
                OneOnOneUI.getInstance().getAppKey(),
                new NERtcCallbackExTemp(),
                null);
        NERtcEx.getInstance().setupLocalVideoCanvas(binding.smallVideo);
        NERtcEx.getInstance().startVideoPreview();
      } catch (Exception e) {
        ALog.e(TAG, "handleInTheVideoCallUI exception:" + e);
      }
    } else {
      binding.textureViewContainer.setVisibility(View.GONE);
      binding.bigVideo.setVisibility(View.VISIBLE);
      NERtcEx.getInstance()
          .setVideoCallback(
              neRtcVideoFrame -> {
                if (isFirstInit) {
                  isFirstInit = false;
                  mFURendererManager.prepareRenderer(mFURendererListener);
                  return false;
                }
                int texId =
                    mFURendererManager.onDrawFrameSingleInput(
                        neRtcVideoFrame.textureId, neRtcVideoFrame.width, neRtcVideoFrame.height);
                if (mSkipFrame-- > 0) {
                  return false;
                }
                if (neRtcVideoFrame.format == NERtcVideoFrame.Format.TEXTURE_OES) {
                  neRtcVideoFrame.format = NERtcVideoFrame.Format.TEXTURE_RGB;
                }
                neRtcVideoFrame.textureId = texId;
                return true;
              },
              true);
    }
  }

  private void setupFaceUnity() {
    mFURendererManager = FURenderer.getInstance();
    mFURendererManager.setMarkFPSEnable(true);
    mFURendererManager.setInputTextureType(FUInputTextureEnum.FU_ADM_FLAG_EXTERNAL_OES_TEXTURE);
    mFURendererManager.setCameraFacing(CameraFacingEnum.CAMERA_FRONT);
    mFURendererManager.setInputOrientation(
        CameraUtils.INSTANCE.getCameraOrientation(Camera.CameraInfo.CAMERA_FACING_FRONT));
    mFURendererManager.setInputTextureMatrix(
        mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
            ? FUTransformMatrixEnum.CCROT0_FLIPVERTICAL
            : FUTransformMatrixEnum.CCROT0);
    mFURendererManager.setInputBufferMatrix(
        mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
            ? FUTransformMatrixEnum.CCROT0_FLIPVERTICAL
            : FUTransformMatrixEnum.CCROT0);
    mFURendererManager.setOutputMatrix(
        mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT
            ? FUTransformMatrixEnum.CCROT0
            : FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
    mFURendererManager.setCreateEGLContext(false);
    mFaceUnityDataFactory = new FaceUnityDataFactory(0);
    mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  @Override
  protected View getRoot(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    binding = FragmentInVideoCallBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  private void setupLocalView(NERtcVideoView videoView) {
    NERtcEx.getInstance().setupLocalVideoCanvas(null);
    NERtcEx.getInstance().enableLocalVideo(true);
    videoView.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
    NERtcEx.getInstance().setupLocalVideoCanvas(videoView);
  }

  @Override
  protected void subscribeUi() {
    super.subscribeUi();
    binding.smallVideo.setZOrderMediaOverlay(true);
    binding.bigVideo.setZOrderMediaOverlay(false);
    binding.bigVideo.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
    viewModel.refresh(activity.getCallParams());
    viewModel
        .getSelfJoinChannelSuccessData()
        .observe(viewLifecycleOwner, aBoolean -> setupLocalView(binding.smallVideo));
    viewModel
        .getOtherInfo()
        .observe(
            viewLifecycleOwner,
            new Observer<OtherUserInfo>() {
              @Override
              public void onChanged(OtherUserInfo otherUserInfo) {
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
                handleUi();
              }
            });
    viewModel
        .getOtherRtcUid()
        .observe(
            viewLifecycleOwner,
            aLong -> {
              rtcCall.setupRemoteView(binding.bigVideo);
            });

    if (virtualCallViewModel != null) {
      virtualCallViewModel
          .getVideoSizeChanged()
          .observe(
              viewLifecycleOwner,
              integerIntegerPair ->
                  handleVideoSizeChanged(integerIntegerPair.first, integerIntegerPair.second));
      virtualCallViewModel
          .getInTheCallDuration()
          .observe(
              viewLifecycleOwner,
              aLong -> binding.tvDuration.setText(TimeUtil.formatSecondTime(aLong)));
    }
  }

  @Override
  protected void handleFloatWindowEvent() {
    refreshUIByFloatWindowEvent();
  }

  private void handleUi() {
    handleSmallVideoUi();
    handleBigVideoUi();
  }

  private void handleBigVideoUi() {
    if (!viewModel.isSelfInSmallUi()) {
      //大屏是自己
      showBigVideoView(viewModel.isLocalCameraIsOpen(), true);
    } else {
      //大屏是对方
      showBigVideoView(viewModel.isRemoteCameraIsOpen(), false);
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
    if (viewModel.isSelfInSmallUi()) {
      //小屏是自己
      if (viewModel.isLocalCameraIsOpen()) {
        // 摄像头打开
        binding.smallVideo.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setVisibility(View.GONE);
      } else {
        // 摄像头关闭
        binding.smallVideo.setVisibility(View.GONE);
        binding.tvSmallVideoDesc.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setText(R.string.already_close_camera);
      }
    } else {
      //小屏是对方
      if (viewModel.isRemoteCameraIsOpen()) {
        // 摄像头打开
        binding.smallVideo.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setVisibility(View.GONE);
      } else {
        // 摄像头关闭
        binding.smallVideo.setVisibility(View.GONE);
        binding.tvSmallVideoDesc.setVisibility(View.VISIBLE);
        binding.tvSmallVideoDesc.setText(R.string.other_already_close_camera);
      }
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
    boolean isSelfInSmallUi = viewModel.isSelfInSmallUi();
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

  @Override
  protected void initEvent() {
    super.initEvent();
    binding.flSmallVideo.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (viewModel.getOtherInfo() == null
                || viewModel.getOtherInfo().getValue() == null
                || TextUtils.isEmpty(viewModel.getOtherInfo().getValue().accId)
                || activity.isVirtualCall()) {
              return;
            }
            viewModel.updateSelfInSmallFlag(!viewModel.isSelfInSmallUi());
            switchVideosCanvas(viewModel.isSelfInSmallUi());
            handleUi();
            handleSecurityVideoMask(securityTipsModel);
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
            handleSwitchCamera();
          }

          @Override
          public void onOpenOrCloseCameraButtonClick() {
            handleOpenOrCloseCamera();
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
    if (activity.isVirtualCall()) {
      binding.floatWindow.setVisibility(View.GONE);
    }
    binding.floatWindow.setOnClickListener(
        v -> {
          activity.doShowFloatingWindow();
        });
    binding.ivEar.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            ToastX.showShortToast(R.string.earphone_tips);
          }
        });
  }

  private void handleOpenOrCloseCamera() {
    if (viewModel.getOtherInfo() == null
        || viewModel.getOtherInfo().getValue() == null
        || TextUtils.isEmpty(viewModel.getOtherInfo().getValue().accId)) {
      return;
    }
    viewModel.doMuteVideo(viewModel.isLocalCameraIsOpen());
    binding
        .bottomBar
        .getViewBinding()
        .ivOpenOrCloseCamera
        .setImageResource(
            viewModel.isLocalCameraIsOpen()
                ? R.drawable.icon_camera_open
                : R.drawable.icon_camera_close);
    handleUi();
  }

  private void handleSwitchCamera() {
    CallUIOperationsMgr.INSTANCE.doSwitchCamera();
  }

  private void handleHangupEvent() {
    ToastX.showShortToast(R.string.end_video);
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
    viewModel.doConfigSpeaker(!viewModel.isSpeakerOn());
    binding
        .bottomBar
        .getViewBinding()
        .ivAudio
        .setImageResource(
            viewModel.isSpeakerOn() ? R.drawable.icon_audio : R.drawable.icon_audio_mute);
  }

  private void handleMircoPhoneEvent() {
    viewModel.doMuteAudio(!viewModel.isMuteLocalAudio());
    if (viewModel.isMuteLocalAudio()) {
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
      rtcCall.setupRemoteView(binding.bigVideo);
    } else {
      setupLocalView(binding.bigVideo);
      rtcCall.setupRemoteView(binding.smallVideo);
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    float x = event.values[0];
    float y = event.values[1];
    if (Math.abs(x) > 3 || Math.abs(y) > 3) {
      if (Math.abs(x) > Math.abs(y)) {
        mFURendererManager.setDeviceOrientation(x > 0 ? 0 : 180);
      } else {
        mFURendererManager.setDeviceOrientation(y > 0 ? 90 : 270);
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    destroyFU();
    binding = null;
    giftRender.release();
  }

  private void destroyFU() {
    mFURendererManager.release();
    isFirstInit = true;
  }
}
