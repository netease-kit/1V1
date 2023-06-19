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
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.fragment.CallFragment;
import com.netease.yunxin.app.oneonone.ui.fragment.InTheAudioCallFragment;
import com.netease.yunxin.app.oneonone.ui.fragment.InTheVideoCallFragment;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.NECallback;
import com.netease.yunxin.app.oneonone.ui.viewmodel.CallViewModel;
import com.netease.yunxin.app.oneonone.ui.viewmodel.VirtualCallViewModel;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.entertainment.common.utils.BluetoothHeadsetUtil;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.EventType;
import com.netease.yunxin.kit.login.model.LoginEvent;
import com.netease.yunxin.kit.login.model.LoginObserver;
import com.netease.yunxin.nertc.nertcvideocall.model.JoinChannelCallBack;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.base.CommonCallActivity;
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
  private final Observer<String> toastObserver = s -> ToastUtils.showLong(s);
  private final Observer<Boolean> switchToInTheNormalCallObserver =
      b -> switchToInTheCallFragment();
  private final Observer<Boolean> playErrorObserver =
      aBoolean -> {
        ToastUtils.showShort(getString(R.string.one_on_one_virtual_call_error));
        finish();
      };
  private final Observer<Boolean> switchInToTheVirtualCallObserver =
      aBoolean -> switchToInTheCallFragment();
  private final Observer<Boolean> releaseAndFinishObserver =
      aBoolean -> {
        ToastUtils.showShort(getString(R.string.one_on_one_virtual_call_end));
        finish();
      };
  private final BluetoothHeadsetUtil.BluetoothHeadsetStatusObserver
      bluetoothHeadsetStatusChangeListener =
          new BluetoothHeadsetUtil.BluetoothHeadsetStatusObserver() {
            @Override
            public void connect() {
              if (!BluetoothHeadsetUtil.hasBluetoothConnectPermission()) {
                BluetoothHeadsetUtil.requestBluetoothConnectPermission();
              }
            }

            @Override
            public void disconnect() {}
          };
  private LoginObserver<LoginEvent> loginObserver =
      new LoginObserver<LoginEvent>() {

        @Override
        public void onEvent(LoginEvent event) {
          if (event.getEventType() == EventType.TYPE_LOGOUT) {
            finish();
          }
        }
      };

  @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
  @Override
  public void doOnCreate(@Nullable Bundle savedInstanceState) {
    adapterStatusBar();
    super.doOnCreate(savedInstanceState);
    callParam = getCallParam();
    if (isVirtualCall() && !callParam.isCalled()) {
      virtualCallViewModel = new ViewModelProvider(this).get(VirtualCallViewModel.class);
      virtualCallViewModel.setCallParam(callParam);
    } else {
      viewModel = new ViewModelProvider(this).get(CallViewModel.class);
    }
    showCallingUI(savedInstanceState, false);
    loadInTheCallFragment(savedInstanceState);
    if (callParam.getChannelType() == ChannelType.AUDIO.getValue()) {
      handlePermission(savedInstanceState, Manifest.permission.RECORD_AUDIO);
    } else if (callParam.getChannelType() == ChannelType.VIDEO.getValue()) {
      handlePermission(
          savedInstanceState, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
    }
    AuthorManager.INSTANCE.registerLoginObserver(loginObserver);
    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
      if (virtualCallViewModel != null) {
        handleVirtualRoomEvent();
      } else if (viewModel != null) {
        handleInTheCallEvent();
        handleToastEvent();
        handleFinishEvent();
      }
    }
  }

  private void loadInTheCallFragment(Bundle savedInstanceState) {
    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
      if (callParam.getChannelType() == ChannelType.VIDEO.getValue()) {
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
    PermissionUtils.permission(permissions)
        .callback(
            new PermissionUtils.FullCallback() {
              @Override
              public void onGranted(@NonNull List<String> granted) {
                if (isFinishing() || isDestroyed()) {
                  return;
                }
                ArrayList<String> list = new ArrayList<>(Arrays.asList(permissions));
                if (granted.containsAll(list)) {
                  if (callParam.isCalled()
                      && getVideoCall().getCurrentState() == CallState.STATE_IDLE) {
                    releaseAndFinish(false);
                    return;
                  }
                  if (!callParam.isCalled()) {
                    showCallingUI(savedInstanceState, true);
                  }
                }
              }

              @Override
              public void onDenied(
                  @NonNull List<String> deniedForever, @NonNull List<String> denied) {
                for (String s : denied) {
                  LogUtil.i(TAG, "onDenied:" + s);
                }
                if (!callParam.isCalled()) {
                  if (denied.size() == 2) {
                    ToastUtils.showLong(R.string.permission_microphone_and_camera_missing_tips);
                  } else if (denied.size() == 1) {
                    String permission = denied.get(0);
                    if (Manifest.permission.CAMERA.equals(permission)) {
                      ToastUtils.showShort(R.string.permission_camera_missing_tips);
                    } else if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
                      ToastUtils.showShort(R.string.permission_microphone_missing_tips);
                    }
                  }
                  new Handler(Looper.getMainLooper())
                      .postDelayed(() -> releaseAndFinish(true), 500);
                }
              }
            })
        .request();
  }

  private void showCallingUI(Bundle savedInstanceState, boolean autoCall) {
    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
      Bundle bundle = new Bundle();
      bundle.putBoolean(AppParams.AUTO_CALL, autoCall);
      getSupportFragmentManager()
          .beginTransaction()
          .setReorderingAllowed(true)
          .replace(R.id.fragment_container_view, CallFragment.class, bundle)
          .commit();
      if (autoCall && virtualCallViewModel != null) {
        virtualCallViewModel.startCountDown();
      }
      BluetoothHeadsetUtil.registerBluetoothHeadsetStatusObserver(
          bluetoothHeadsetStatusChangeListener);
      if (BluetoothHeadsetUtil.isBluetoothHeadsetConnected()
          && !BluetoothHeadsetUtil.hasBluetoothConnectPermission()) {
        BluetoothHeadsetUtil.requestBluetoothConnectPermission();
      }
    }
  }

  @Override
  protected int provideLayoutId() {
    return R.layout.activity_call;
  }

  @NonNull
  @Override
  protected NERTCCallingDelegate provideRtcDelegate() {
    return null;
  }

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
        new JoinChannelCallBack() {
          @Override
          public void onJoinChannel(ChannelFullInfo channelFullInfo) {
            LogUtil.i(TAG, "rtcCall onJoinChannel");
            callback.onSuccess(channelFullInfo);
          }

          @Override
          public void onJoinFail(String msg, int code) {
            LogUtil.e(TAG, "rtcCall,onJoinFail msg:" + msg + ",code:" + code);
            callback.onError(code, msg);
          }
        });
  }

  public void rtcAccept() {
    doAccept(
        new JoinChannelCallBack() {
          @Override
          public void onJoinChannel(ChannelFullInfo channelFullInfo) {
            LogUtil.i(TAG, "rtcAccept onJoinChannel");
          }

          @Override
          public void onJoinFail(String msg, int code) {
            LogUtil.e(TAG, "rtcAccept,onJoinFail msg:" + msg + ",code:" + code);
            ToastUtils.showShort(getString(R.string.one_on_one_network_error));
          }
        });
  }

  public void rtcHangup(NECallback<Integer> callback) {
    doHangup(
        new RequestCallbackWrapper<Void>() {
          @Override
          public void onResult(int code, Void result, Throwable exception) {
            LogUtil.i(TAG, "rtcHangup,code:" + code + ",exception:" + exception);
            callback.onSuccess(code);
          }
        });
  }

  public CallParam getCallParams() {
    return callParam;
  }

  public NERTCVideoCall getRtcCall() {
    return getVideoCall();
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
    AuthorManager.INSTANCE.unregisterLoginObserver(loginObserver);
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
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    getOnBackPressedDispatcher().onBackPressed();
  }

  private void stopRing() {
    AVChatSoundPlayer.Companion.instance().stop(CallActivity.this);
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
}
