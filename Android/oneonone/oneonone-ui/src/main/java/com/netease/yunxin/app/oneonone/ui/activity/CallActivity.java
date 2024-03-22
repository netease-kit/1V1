// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.activity;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.fragment.CallFragment;
import com.netease.yunxin.app.oneonone.ui.fragment.InTheAudioCallFragment;
import com.netease.yunxin.app.oneonone.ui.fragment.InTheVideoCallFragment;
import com.netease.yunxin.app.oneonone.ui.utils.HighKeepAliveUtil;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CallViewModel;
import com.netease.yunxin.app.oneonone.ui.viewmodel.VirtualCallViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.kit.call.NEResultObserver;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallInfo;
import com.netease.yunxin.kit.common.ui.utils.Permission;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.utils.BluetoothHeadsetUtil;
import com.netease.yunxin.nertc.nertcvideocall.bean.CommonResult;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.base.CommonCallActivity;
import com.netease.yunxin.nertc.ui.p2p.P2PUIConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/** 呼叫页&&通话页 https://dev.yunxin.163.com/docs/interface/NERTCCallkit/Latest/Android/html/ */
public class CallActivity extends CommonCallActivity {
  private static final String TAG = "CallActivity";
  private CallParam callParam;
  private CallViewModel viewModel;
  private VirtualCallViewModel virtualCallViewModel;
  private Fragment inTheCallFragment;
  private final Observer<Boolean> callFinishObserver = aBoolean -> finish();
  private final Observer<String> toastObserver = s -> ToastX.showShortToast(s);
  private final Observer<Boolean> switchToInTheNormalCallObserver =
      b -> switchToInTheCallFragment();
  private final Observer<Boolean> playErrorObserver =
      aBoolean -> {
        ToastX.showShortToast(getString(R.string.one_on_one_virtual_call_error));
        finish();
      };
  private final Observer<Boolean> switchInToTheVirtualCallObserver =
      aBoolean -> switchToInTheCallFragment();
  private final Observer<Boolean> releaseAndFinishObserver =
      aBoolean -> {
        ToastX.showShortToast(getString(R.string.one_on_one_virtual_call_end));
        finish();
      };
  private final BluetoothHeadsetUtil.BluetoothHeadsetStatusObserver
      bluetoothHeadsetStatusChangeListener =
          new BluetoothHeadsetUtil.BluetoothHeadsetStatusObserver() {
            @Override
            public void connect() {
              if (!BluetoothHeadsetUtil.hasBluetoothConnectPermission(CallActivity.this)) {
                BluetoothHeadsetUtil.requestBluetoothConnectPermission(CallActivity.this);
              }
            }

            @Override
            public void disconnect() {}
          };

  private final com.netease.nimlib.sdk.Observer<StatusCode> imOnlineStatusObserver =
      statusCode -> {
        if (statusCode == StatusCode.KICKOUT) {
          finish();
        }
      };

  @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void doOnCreate(@Nullable Bundle savedInstanceState) {
    adapterStatusBar();
    super.doOnCreate(savedInstanceState);
    if (!CallKitUI.INSTANCE.getInit()) {
      ALog.e(TAG, "CallKitUI not init");
      finish();
      return;
    }
    callParam = getCallParam();
    if (isVirtualCall() && !callParam.isCalled()) {
      virtualCallViewModel = new ViewModelProvider(this).get(VirtualCallViewModel.class);
      virtualCallViewModel.setCallParam(callParam);
    } else {
      viewModel = new ViewModelProvider(this).get(CallViewModel.class);
    }
    if (!isFromFloatWindow()) {
      showCallingUI(savedInstanceState, false);
      if (callParam.getCallType() == ChannelType.AUDIO.getValue()) {
        handlePermission(savedInstanceState, Manifest.permission.RECORD_AUDIO);
      } else if (callParam.getCallType() == ChannelType.VIDEO.getValue()) {
        handlePermission(
            savedInstanceState, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
      }
    }
    loadInTheCallFragment(savedInstanceState);
    NIMClient.getService(AuthServiceObserver.class)
        .observeOnlineStatus(imOnlineStatusObserver, true);
    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
      if (virtualCallViewModel != null) {
        handleVirtualRoomEvent();
      } else if (viewModel != null) {
        handleInTheCallEvent();
        handleToastEvent();
        handleFinishEvent();
      }
    }
    HighKeepAliveUtil.openHighKeepAlive(this, OneOnOneUI.getInstance().getAppKey());
    BluetoothHeadsetUtil.registerBluetoothHeadsetStatusObserver(
        bluetoothHeadsetStatusChangeListener);
    if (BluetoothHeadsetUtil.isBluetoothHeadsetConnected()
        && !BluetoothHeadsetUtil.hasBluetoothConnectPermission(CallActivity.this)) {
      BluetoothHeadsetUtil.requestBluetoothConnectPermission(CallActivity.this);
    }
  }

  @Nullable
  @Override
  protected P2PUIConfig provideUIConfig(@Nullable CallParam callParam) {
    ALog.d(TAG, new ParameterMap("provideUIConfig").append("param", callParam).toValue());
    return new P2PUIConfig.Builder()
        //        .enableForegroundService(true)
        .foregroundNotificationConfig(new CallKitNotificationConfig(R.mipmap.ic_launcher))
        .enableFloatingWindow(true)
        .enableAutoFloatingWindowWhenHome(true)
        .enableVirtualBlur(true)
        .build();
  }

  private void loadInTheCallFragment(Bundle savedInstanceState) {
    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
      if (callParam.getCallType() == ChannelType.VIDEO.getValue()) {
        inTheCallFragment = new InTheVideoCallFragment();
      } else {
        inTheCallFragment = new InTheAudioCallFragment();
      }
      getSupportFragmentManager()
          .beginTransaction()
          .setReorderingAllowed(true)
          .replace(R.id.fragment_in_the_call, inTheCallFragment)
          .commit();
    }
  }

  private void handleVirtualRoomEvent() {
    virtualCallViewModel.getSwitchToInTheCall().observeForever(switchInToTheVirtualCallObserver);
    virtualCallViewModel.getReleaseAndFinish().observeForever(releaseAndFinishObserver);
    virtualCallViewModel.getPlayError().observeForever(playErrorObserver);
  }

  private void handleInTheCallEvent() {
    viewModel.getSwitchToInTheCall().observeForever(switchToInTheNormalCallObserver);
  }

  private void handleFinishEvent() {
    viewModel.getCallFinished().observeForever(callFinishObserver);
  }

  private void handleToastEvent() {
    viewModel.getToastData().observeForever(toastObserver);
  }

  private void handlePermission(Bundle savedInstanceState, String... permissions) {
    Permission.requirePermissions(CallActivity.this, permissions)
        .request(
            new Permission.PermissionCallback() {
              @Override
              public void onGranted(@NonNull List<String> granted) {
                if (isFinishing() || isDestroyed()) {
                  return;
                }
                ArrayList<String> list = new ArrayList<>(Arrays.asList(permissions));
                if (granted.containsAll(list)) {
                  if (callParam.isCalled() && currentCallState() == CallState.STATE_IDLE) {
                    releaseAndFinish(false);
                    return;
                  }
                  if (!callParam.isCalled()) {
                    showCallingUI(savedInstanceState, true);
                  }
                }
              }

              @Override
              public void onDenial(
                  List<String> permissionsDenial, List<String> permissionDenialForever) {
                for (String s : permissionsDenial) {
                  LogUtil.i(TAG, "onDenied:" + s);
                }
                if (!callParam.isCalled()) {
                  if (permissionsDenial.size() == 2 || permissionDenialForever.size() == 2) {
                    ToastX.showShortToast(R.string.permission_microphone_and_camera_missing_tips);
                  } else if (permissionsDenial.size() == 1 || permissionDenialForever.size() == 1) {
                    String permission = "";
                    if (permissionsDenial.size() == 1) {
                      permission = permissionsDenial.get(0);
                    } else {
                      permission = permissionDenialForever.get(0);
                    }
                    if (Manifest.permission.CAMERA.equals(permission)) {
                      ToastX.showShortToast(R.string.permission_camera_missing_tips);
                    } else if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
                      ToastX.showShortToast(R.string.permission_microphone_missing_tips);
                    }
                  }
                  new Handler(Looper.getMainLooper())
                      .postDelayed(() -> releaseAndFinish(true), 500);
                }
              }

              @Override
              public void onException(Exception exception) {}
            });
  }

  private void showCallingUI(Bundle savedInstanceState, boolean autoCall) {
    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
      Bundle bundle = new Bundle();
      bundle.putBoolean(AppParams.AUTO_CALL, autoCall);
      getSupportFragmentManager()
          .beginTransaction()
          .setReorderingAllowed(true)
          .replace(R.id.fragment_container_view, CallFragment.class, bundle)
          .commitAllowingStateLoss();
      if (autoCall && virtualCallViewModel != null) {
        virtualCallViewModel.startCountDown();
      }
    }
  }

  @Override
  protected int provideLayoutId() {
    return R.layout.activity_call;
  }

  @NonNull
  public void switchToInTheCallFragment() {
    stopRing();
    findViewById(R.id.fragment_in_the_call).setVisibility(View.VISIBLE);
    findViewById(R.id.fragment_container_view).setVisibility(View.GONE);
    if (inTheCallFragment instanceof InTheVideoCallFragment) {
      ((InTheVideoCallFragment) inTheCallFragment).handleInTheVideoCallUI();
    }
    if (isVirtualCall() && virtualCallViewModel != null && !callParam.isCalled()) {
      virtualCallViewModel.getPlayer().prepareAsync();
    }
  }

  public void rtcCall(NECallback<ChannelFullInfo> callback) {
    doCall(
        new NEResultObserver<CommonResult<NECallInfo>>() {
          @Override
          public void onResult(CommonResult<NECallInfo> neCallInfoCommonResult) {
            if (neCallInfoCommonResult.isSuccessful()) {
              LogUtil.i(TAG, "rtcCall success");
            } else {
              LogUtil.e(TAG, "rtcCall fail:" + neCallInfoCommonResult.msg);
              callback.onError(neCallInfoCommonResult.code, neCallInfoCommonResult.msg);
            }
          }
        });
  }

  public void rtcAccept() {
    doAccept(
        new NEResultObserver<CommonResult<NECallInfo>>() {
          @Override
          public void onResult(CommonResult<NECallInfo> neCallInfoCommonResult) {
            if (neCallInfoCommonResult.isSuccessful()) {
              LogUtil.i(TAG, "rtcAccept success");
            } else {
              LogUtil.e(TAG, "rtcAccept fail:" + neCallInfoCommonResult.msg);
              ToastX.showShortToast(getString(R.string.one_on_one_network_error));
            }
          }
        });
  }

  public void rtcHangup(NECallback<Integer> callback) {
    doHangup(
        new NEResultObserver<CommonResult<Void>>() {
          @Override
          public void onResult(CommonResult<Void> voidCommonResult) {
            //                 LogUtil.i(TAG, "rtcHangup,code:" + code + ",exception:" + exception);
            if (callback != null) {
              callback.onSuccess(voidCommonResult.code);
            }
          }
        });
  }

  public CallParam getCallParams() {
    return callParam;
  }

  public NECallEngine getRtcCall() {
    return getCallEngine();
  }

  @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
  private void adapterStatusBar() {
    // 5.0以上系统状态栏透明
    Window window = getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    //会让应用的主体内容占用系统状态栏的空间
    window
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    //将状态栏设置成透明色
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.setStatusBarColor(Color.TRANSPARENT);
    }
  }

  @Override
  public void finish() {
    stopRing();
    super.finish();
  }

  @Override
  protected void onDestroy() {
    NIMClient.getService(AuthServiceObserver.class)
        .observeOnlineStatus(imOnlineStatusObserver, false);
    BluetoothHeadsetUtil.unregisterBluetoothHeadsetStatusObserver(
        bluetoothHeadsetStatusChangeListener);
    if (viewModel != null) {
      viewModel.getSwitchToInTheCall().removeObserver(switchToInTheNormalCallObserver);
      viewModel.getToastData().removeObserver(toastObserver);
      viewModel.getCallFinished().removeObserver(callFinishObserver);
    }
    if (virtualCallViewModel != null) {
      virtualCallViewModel.getReleaseAndFinish().removeObserver(releaseAndFinishObserver);
      virtualCallViewModel.getSwitchToInTheCall().removeObserver(switchInToTheVirtualCallObserver);
      virtualCallViewModel.getPlayError().removeObserver(playErrorObserver);
    }
    HighKeepAliveUtil.closeHighKeepAlive(this);
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    getOnBackPressedDispatcher().onBackPressed();
  }

  private void stopRing() {
    AVChatSoundPlayer.stop(CallActivity.this);
  }

  public boolean isVirtualCall() {
    try {
      if (CallConfig.enableVirtualCall
          && callParam.getCallExtraInfo() != null
          && new JSONObject(callParam.getCallExtraInfo()).getBoolean(AppParams.CALLED_IS_VIRTUAL)) {
        return true;
      }
    } catch (JSONException e) {
      ALog.e(TAG, "isVirtualCall json parse exception:" + e);
    }
    return false;
  }

  public void doShowFloatingWindow() {
    super.doShowFloatingWindow();
  }

  public boolean isFromFloatWindow() {
    return super.isFromFloatingWindow();
  }
}
