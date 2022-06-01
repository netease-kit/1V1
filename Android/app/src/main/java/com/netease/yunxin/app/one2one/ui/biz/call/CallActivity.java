/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.ui.biz.call;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.constant.AppParams;
import com.netease.yunxin.app.one2one.constant.CallConfig;
import com.netease.yunxin.app.one2one.ui.biz.call.viewmodel.CallViewModel;
import com.netease.yunxin.app.one2one.ui.biz.call.viewmodel.PstnCallViewModel;
import com.netease.yunxin.app.one2one.ui.dialog.PermissionTipDialog;
import com.netease.yunxin.app.one2one.utils.CallTimeOutHelper;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.NECallback;
import com.netease.yunxin.nertc.nertcvideocall.model.JoinChannelCallBack;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCCallingDelegate;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.base.CommonCallActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 呼叫页&&通话页
 * https://dev.yunxin.163.com/docs/interface/NERTCCallkit/Latest/Android/html/
 */
public class CallActivity extends CommonCallActivity {
    private static final String TAG = "CallActivity";
    private PermissionTipDialog dialog = null;
    private CallParam callParam;
    private CallViewModel viewModel;
    private PstnCallViewModel pstnCallViewModel;

    @Override
    public void doOnCreate(@Nullable Bundle savedInstanceState) {
        adapterStatusBar();
        super.doOnCreate(savedInstanceState);
        callParam = getCallParam();
        viewModel = new ViewModelProvider(this).get(CallViewModel.class);
        if (needPstnCall()){
            pstnCallViewModel = new ViewModelProvider(this).get(PstnCallViewModel.class);
        }
        if (!PermissionUtils.isGranted(
                PermissionConstants.CAMERA,
                PermissionConstants.MICROPHONE
        )) {
            dialog = new PermissionTipDialog(this, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToastUtils.showShort(R.string.permission_request_failed_tips);
                    releaseAndFinish(true);
                }
            });
            dialog.show();
        }
        PermissionUtils.permission(
                PermissionConstants.CAMERA,
                PermissionConstants.MICROPHONE
        ).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                ArrayList<String> list = new ArrayList<>();
                list.add(Manifest.permission.CAMERA);
                list.add(Manifest.permission.RECORD_AUDIO);
                if (granted.containsAll(list)) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (callParam.isCalled() && getVideoCall().getCurrentState() == CallState.STATE_IDLE) {
                        releaseAndFinish(false);
                        return;
                    }
                    if (savedInstanceState == null && !getSupportFragmentManager().isDestroyed()) {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .add(R.id.fragment_container_view, CallFragment.class, null)
                                .commit();
                        viewModel.getSwitchToInTheCall().observe(CallActivity.this, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean b) {
                                switchToInTheCallFragment();
                            }
                        });
                    }
                }
            }

            @Override
            public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                for (String s : denied) {
                    LogUtil.i(TAG, "onDenied:" + s);
                }
                ToastUtils.showShort(R.string.permission_request_failed_tips);
                releaseAndFinish(true);
            }
        }).request();
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

    protected void switchToInTheCallFragment() {
        stopRing();
        if (getSupportFragmentManager().isDestroyed()) {
            return;
        }
        Fragment inTheCallFragment = null;
        if (callParam.getChannelType() == ChannelType.VIDEO.getValue()) {
            inTheCallFragment = new InTheVideoCallFragment();
        } else {
            inTheCallFragment = new InTheAudioCallFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, inTheCallFragment)
                .commit();
    }

    protected void rtcCall(NECallback<ChannelFullInfo> callback) {
        doCall(new JoinChannelCallBack() {
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

    protected void rtcAccept() {
        doAccept(new JoinChannelCallBack() {
            @Override
            public void onJoinChannel(ChannelFullInfo channelFullInfo) {
                LogUtil.i(TAG, "rtcAccept onJoinChannel");
            }

            @Override
            public void onJoinFail(String msg, int code) {
                LogUtil.e(TAG, "rtcAccept,onJoinFail msg:" + msg + ",code:" + code);
            }
        });
    }

    protected void rtcHangup(NECallback<Integer> callback) {
        doHangup(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                LogUtil.i(TAG, "rtcHangup,code:" + code + ",exception:" + exception);
                callback.onSuccess(code);
            }
        });
    }

    protected CallParam getCallParams() {
        return callParam;
    }

    protected NERTCVideoCall getRtcCall() {
        return getVideoCall();
    }

    private void adapterStatusBar() {
        // 5.0以上系统状态栏透明
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //会让应用的主体内容占用系统状态栏的空间
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //将状态栏设置成透明色
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    @Override
    public void finish() {
        stopRing();
        CallTimeOutHelper.configTimeOut(CallConfig.CALL_TOTAL_WAIT_TIMEOUT, CallConfig.CALL_TOTAL_WAIT_TIMEOUT);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        getOnBackPressedDispatcher().onBackPressed();
    }

    private void stopRing() {
        AVChatSoundPlayer.Companion.instance().stop(CallActivity.this);
    }

    private boolean needPstnCall(){
        try {
            if (callParam.getChannelType()==ChannelType.AUDIO.getValue()&&callParam.getCallExtraInfo()!=null&&new JSONObject(callParam.getCallExtraInfo()).getBoolean(AppParams.NEED_PSTN_CALL)){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
