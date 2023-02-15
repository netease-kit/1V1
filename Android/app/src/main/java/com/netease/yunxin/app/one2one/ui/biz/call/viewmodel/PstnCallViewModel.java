// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.call.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.utils.AccountAmountHelper;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackExTemp;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackProxyMgr;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.state.CallState;
import com.netease.yunxin.nertc.pstn.base.AbsPstnCallback;
import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;
import com.netease.yunxin.nertc.ui.base.ResultInfo;
import com.netease.yunxin.nertc.ui.utils.SecondsTimer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class PstnCallViewModel extends AndroidViewModel {
  private static final String TAG = "PstnCallViewModel";
  private MutableLiveData<Boolean> releaseAndFinish = new MutableLiveData<>();
  private MutableLiveData<String> pstnToastData = new MutableLiveData<>();
  private MutableLiveData<Integer> switchToInTheCall = new MutableLiveData<>();
  private MutableLiveData<Boolean> rtcCallResult = new MutableLiveData<>();
  private SecondsTimer inThePstnCallSecondTimer;
  private long pstnCallUsedDuration;
  private static final int REJECT_CODE = 1000;
  private boolean hangup = false;
  private MutableLiveData<Long> otherRtcUid = new MutableLiveData<>();
  private final MutableLiveData<Boolean> sendSmsData = new MutableLiveData<>();

  public MutableLiveData<Boolean> getReleaseAndFinish() {
    return releaseAndFinish;
  }

  public MutableLiveData<String> getPstnToastData() {
    return pstnToastData;
  }

  public MutableLiveData<Integer> getSwitchToInTheCall() {
    return switchToInTheCall;
  }

  public MutableLiveData<Boolean> getRtcCallResult() {
    return rtcCallResult;
  }

  public MutableLiveData<Long> getOtherRtcUid() {
    return otherRtcUid;
  }

  public MutableLiveData<Boolean> getSendSmsData() {
    return sendSmsData;
  }

  private NERtcCallbackExTemp rtcCallback =
      new NERtcCallbackExTemp() {
        @Override
        public void onUserJoined(long l) {
          super.onUserJoined(l);
          otherRtcUid.postValue(l);
        }
      };

  private AbsPstnCallback pstnCallback =
      new AbsPstnCallback() {

        @Override
        public void onDirectCallAccept(int code) {
          LogUtil.i(TAG, "onDirectCallAccept,code:" + code);
          startInTheCallTimer();
          switchToInTheCall.postValue(ChannelType.AUDIO.getValue());
        }

        @Override
        public void onDirectCallDisconnectWithError(int code, @Nullable String errorMsg) {
          LogUtil.i(TAG, "onDirectCallDisconnectWithError,code:" + code + ",errorMsg:" + errorMsg);
          releaseAndFinish.postValue(true);
        }

        @Override
        public void onDirectCallHangupWithReason(
            int reason, int code, @Nullable String errorMsg, boolean isCallEstablished) {
          LogUtil.i(
              TAG,
              "onDirectCallHangupWithReason:reason:"
                  + reason
                  + ",code:"
                  + code
                  + ",errorMsg:"
                  + errorMsg
                  + ",isCallEstablished:"
                  + isCallEstablished);
          if (!hangup && reason == REJECT_CODE && !isCallEstablished) {
            //对方拒接，自己挂断
            pstnToastData.postValue(getApplication().getString(R.string.reject_tips));
          }
          releaseAndFinish.postValue(true);
        }

        @Override
        public void onTimeOutWithPstn() {
          super.onTimeOutWithPstn();
          LogUtil.i(TAG, "onTimeOutWithPstn");
          releaseAndFinish.postValue(true);
          sendSmsData.postValue(true);
        }

        @Override
        public void onDirectCallRing(int code) {
          LogUtil.i(TAG, "onDirectCallRing,code:" + code);
        }

        @Override
        public void onDirectStartCall(int code, @Nullable String errorMsg) {
          LogUtil.i(TAG, "onDirectStartCall,code:" + code + ",errorMsg:" + errorMsg);
        }

        @Override
        public void onRtcCallResult(@NonNull ResultInfo<ChannelFullInfo> result) {
          LogUtil.i(TAG, "onRtcCallHangupResult,result:" + result);
          rtcCallResult.postValue(true);
          if (result.getSuccess()) {
            return;
          }

          if (result.getMsg() != null
              && result.getMsg().getCode() == ResponseCode.RES_PEER_NIM_OFFLINE) {
            return;
          }
          pstnToastData.postValue(getApplication().getString(R.string.call_failed));
          if (NERTCVideoCall.sharedInstance().getCurrentState() == CallState.STATE_IDLE) {
            releaseAndFinish.postValue(true);
          }
        }

        @Override
        public void onTransError(@NonNull ResultInfo<?> result) {
          LogUtil.i(TAG, "onTransError,result:" + result);
          releaseAndFinish.postValue(true);
        }
      };

  public PstnCallViewModel(@NonNull Application application) {
    super(application);
    LogUtil.i(TAG, "add pstnCallback");
    PstnFunctionMgr.addCallback(pstnCallback);
    NERtcCallbackProxyMgr.getInstance().addCallback(rtcCallback);
  }

  public void startInTheCallTimer() {
    if (inThePstnCallSecondTimer == null) {
      inThePstnCallSecondTimer = new SecondsTimer(0, 1000);
    }
    inThePstnCallSecondTimer.start(
        new Function1<Long, Unit>() {
          @Override
          public Unit invoke(Long aLong) {
            LogUtil.i(TAG, "startInTheCallTimer aLong:" + aLong);
            pstnCallUsedDuration = aLong;
            return null;
          }
        });
  }

  public void cancelInTheTimer() {
    if (inThePstnCallSecondTimer != null) {
      inThePstnCallSecondTimer.cancel();
      inThePstnCallSecondTimer = null;
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    LogUtil.i(TAG, "remove pstnCallback");
    PstnFunctionMgr.removeCallback(pstnCallback);
    NERtcCallbackProxyMgr.getInstance().removeCallback(rtcCallback);
    cancelInTheTimer();
    AccountAmountHelper.addPstnUsedDurationWithAccount(
        UserInfoManager.getSelfImAccid(), pstnCallUsedDuration);
    hangup = false;
  }

  public void hangup() {
    hangup = true;
  }
}
