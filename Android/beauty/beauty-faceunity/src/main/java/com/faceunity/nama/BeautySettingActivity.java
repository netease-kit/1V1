// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.Nullable;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.databinding.ActivityBeautySettingBinding;
import com.faceunity.nama.listener.FURendererListener;
import com.netease.lava.api.IVideoRender;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.video.NERtcVideoFrame;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import java.util.concurrent.CountDownLatch;

public class BeautySettingActivity extends BaseActivity implements SensorEventListener {

  private static final String TAG = "BeautySettingActivity";
  public static final String INTENT_KEY_APP_KEY = "intent_key_app_key";
  private ActivityBeautySettingBinding binding;
  private boolean isFirstInit = true;
  private boolean isFUOn = false;
  private Handler mHandler;
  private int mSkipFrame = 5;
  private boolean isBeautySettingPanelShowing;
  private String appKey;
  private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
  private FURenderer mFURendererManager;
  private FaceUnityDataFactory mFaceUnityDataFactory;
  private SensorManager mSensorManager;

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityBeautySettingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initIntent();
    setupFaceUnity();
    initRTC();
    initViews();
    startPreview();
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

  private void initIntent() {
    appKey = getIntent().getStringExtra(INTENT_KEY_APP_KEY);
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
    binding.fuView.bindDataFactory(mFaceUnityDataFactory);
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
  }

  private void initRTC() {

    try {
      NERtcEx.getInstance().release();
      NERtcEx.getInstance()
          .init(BeautySettingActivity.this, appKey, new NERtcCallbackWrapper(), null);
      NERtcEx.getInstance()
          .setVideoCallback(
              neRtcVideoFrame -> {
                if (isFirstInit) {
                  isFirstInit = false;
                  mHandler = new Handler(Looper.myLooper());
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
    } catch (Exception e) {
      ALog.e(TAG, "initRTC exception", e);
    }
  }

  private void startPreview() {
    binding.vvLocalUser.setZOrderMediaOverlay(true);
    binding.vvLocalUser.setScalingType(IVideoRender.ScalingType.SCALE_ASPECT_FILL);
    NERtcEx.getInstance().setupLocalVideoCanvas(binding.vvLocalUser);
    NERtcEx.getInstance().startVideoPreview();
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initViews() {
    binding.switchCamera.setOnClickListener(v -> NERtcEx.getInstance().switchCamera());
    binding.beautyOk.setOnClickListener(
        v -> {
          finish();
        });
    binding.openBeauty.setOnClickListener(v -> showOrDismissBeautySettingPanel());
    binding.vvLocalUser.setOnClickListener(
        v -> {
          if (isBeautySettingPanelShowing) {
            showOrDismissBeautySettingPanel();
          }
        });
  }

  private void showOrDismissBeautySettingPanel() {
    isBeautySettingPanelShowing = !isBeautySettingPanelShowing;
    binding.beautySettingPanel.setVisibility(
        isBeautySettingPanelShowing ? View.VISIBLE : View.INVISIBLE);
    binding.beautyToolsPanel.setVisibility(
        isBeautySettingPanelShowing ? View.INVISIBLE : View.VISIBLE);
    binding.fuView.checkSkinBeautyTab();
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

  private void destroyFU() {
    if (mHandler == null) {
      return;
    }
    CountDownLatch countDownLatch = new CountDownLatch(1);
    mHandler.post(
        new Runnable() {
          @Override
          public void run() {
            mFURendererManager.release();
            isFirstInit = true;
            countDownLatch.countDown();
          }
        });
    try {
      mHandler = null;
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    if (mSensorManager != null) {
      mSensorManager.unregisterListener(this);
    }
    destroyFU();
    NERtcEx.getInstance().stopVideoPreview();
    NERtcEx.getInstance().release();
    super.onDestroy();
  }
}
