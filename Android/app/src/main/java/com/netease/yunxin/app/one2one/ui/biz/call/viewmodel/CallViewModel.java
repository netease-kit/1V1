// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.call.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.constant.AppParams;
import com.netease.yunxin.app.one2one.model.OtherUserInfo;
import com.netease.yunxin.app.one2one.ui.biz.call.NERtcCallDelegate;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.SecurityAuditManager;
import com.netease.yunxin.app.one2one.utils.security.SecurityAuditModel;
import com.netease.yunxin.app.one2one.utils.security.SecurityFoulUser;
import com.netease.yunxin.app.one2one.utils.security.SecurityTipsModel;
import com.netease.yunxin.app.one2one.utils.security.SecurityType;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackExTemp;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackProxyMgr;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.utils.SecondsTimer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.json.JSONException;
import org.json.JSONObject;

public class CallViewModel extends AndroidViewModel {
  private static final String TAG = "CallViewModel";
  private MutableLiveData<OtherUserInfo> otherUserInfo;
  private SecurityAuditManager securityAuditManager = new SecurityAuditManager();
  private MutableLiveData<SecurityTipsModel> securityTipsModel;
  private SecondsTimer inTheCallSecondTimer;
  private final MutableLiveData<Long> inTheCallDuration = new MutableLiveData<>();
  private long selfRtcUid;
  private OtherUserInfo userInfo;
  private CallParam callParam;
  private boolean needPstnCall = false;
  private final MutableLiveData<Boolean> switchToInTheCall = new MutableLiveData<>();
  private final MutableLiveData<String> toastData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> callFinished = new MutableLiveData<>();
  private final MutableLiveData<AVChatSoundPlayer.RingerTypeEnum> playRing =
      new MutableLiveData<>();
  private final MutableLiveData<Boolean> remoteVideoMute = new MutableLiveData<>();
  private final MutableLiveData<Boolean> sendSmsData = new MutableLiveData<>();
  private final MutableLiveData<Long> otherRtcUid = new MutableLiveData<>();
  private NERtcCallbackExTemp rtcCallback =
      new NERtcCallbackExTemp() {
        @Override
        public void onJoinChannel(int i, long l, long l1, long l2) {
          super.onJoinChannel(i, l, l1, l2);
          selfRtcUid = l2;
        }

        @Override
        public void onUserJoined(long l) {
          super.onUserJoined(l);
          otherRtcUid.postValue(l);
        }
      };

  private final NERtcCallDelegate neRtcCallDelegate =
      new NERtcCallDelegate() {

        @Override
        public void onFirstVideoFrameDecoded(@Nullable String userId, int width, int height) {
          super.onFirstVideoFrameDecoded(userId, width, height);
          switchToInTheCall.postValue(true);
        }

        @Override
        public void onVideoMuted(String userId, boolean isMuted) {
          super.onVideoMuted(userId, isMuted);
          remoteVideoMute.postValue(isMuted);
        }

        @Override
        public void onUserEnter(@Nullable String userId) {
          super.onUserEnter(userId);
          LogUtil.i(TAG, "onUserEnter,userId:" + userId);
          if (callParam.getChannelType() == ChannelType.AUDIO.getValue()) {
            switchToInTheCall.postValue(true);
          }
          startInTheCallTimer();
        }

        @Override
        public void onCallEnd(@Nullable String userId) {
          super.onCallEnd(userId);
          LogUtil.i(TAG, "onCallEnd,userId:" + userId);
          cancelInTheCallTimer();
        }

        @Override
        public void onCallFinished(@Nullable int code, @Nullable String msg) {
          super.onCallFinished(code, msg);
          LogUtil.i(TAG, "onCallFinished,code:" + code + ",msg:" + msg);
          callFinished.postValue(true);
        }

        @Override
        public void onRejectByUserId(@Nullable String userId) {
          if (!callParam.isCalled()) {
            toastData.postValue(getApplication().getString(R.string.reject_tips));
            playRing.postValue(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);
          }
          super.onRejectByUserId(userId);
          LogUtil.i(TAG, "onRejectByUserId,userId:" + userId);
        }

        @Override
        public void onUserBusy(@Nullable String userId) {
          if (!callParam.isCalled()) {
            toastData.postValue(getApplication().getString(R.string.user_busy));
            playRing.postValue(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
          }
          super.onUserBusy(userId);
          LogUtil.i(TAG, "onUserBusy,userId:" + userId);
        }

        @Override
        public void onCancelByUserId(@Nullable String userId) {
          if (callParam.isCalled()) {
            toastData.postValue(getApplication().getString(R.string.cancel_by_other));
          }
          super.onCancelByUserId(userId);
          LogUtil.i(TAG, "onCancelByUserId,userId:" + userId);
        }

        @Override
        public void timeOut() {
          LogUtil.i(TAG, "timeOut");
          if (needPstnCall) {
            if (callParam.isCalled()) {
              toastData.postValue(getApplication().getString(R.string.called_timeout_tips));
              super.timeOut();
            }
          } else {
            if (callParam.isCalled()) {
              toastData.postValue(getApplication().getString(R.string.called_timeout_tips));
            } else {
              toastData.postValue(getApplication().getString(R.string.caller_timeout_tips));
              sendSmsData.postValue(true);
            }
            super.timeOut();
          }
        }
      };

  public CallViewModel(@NonNull Application application) {
    super(application);
    NERTCVideoCall.sharedInstance().addDelegate(neRtcCallDelegate);
    NERtcCallbackProxyMgr.getInstance().addCallback(rtcCallback);
  }

  public MutableLiveData<Boolean> getSwitchToInTheCall() {
    return switchToInTheCall;
  }

  public MutableLiveData<String> getToastData() {
    return toastData;
  }

  public MutableLiveData<Boolean> getCallFinished() {
    return callFinished;
  }

  public MutableLiveData<AVChatSoundPlayer.RingerTypeEnum> getPlayRing() {
    return playRing;
  }

  public MutableLiveData<SecurityTipsModel> getSecurityTipsModel() {
    return securityTipsModel;
  }

  public MutableLiveData<Long> getInTheCallDuration() {
    return inTheCallDuration;
  }

  public MutableLiveData<Boolean> getRemoteVideoMute() {
    return remoteVideoMute;
  }

  public MutableLiveData<Boolean> getSendSmsData() {
    return sendSmsData;
  }

  public MutableLiveData<Long> getOtherRtcUid() {
    return otherRtcUid;
  }

  public void refresh(CallParam callParam) {
    try {
      LogUtil.i(TAG, "callParam:" + callParam);
      this.callParam = callParam;
      JSONObject jsonObject = new JSONObject(callParam.getCallExtraInfo());
      userInfo = new OtherUserInfo();
      userInfo.isCalled = callParam.isCalled();
      userInfo.callType = callParam.getChannelType();
      needPstnCall = jsonObject.getBoolean(AppParams.NEED_PSTN_CALL);
      if (callParam.isCalled()) {
        userInfo.nickname = jsonObject.getString(AppParams.CALLER_USER_NAME);
        userInfo.mobile = jsonObject.getString(AppParams.CALLER_USER_MOBILE);
        userInfo.avatar = jsonObject.getString(AppParams.CALLER_USER_AVATAR);
        if (callParam.getChannelType() == ChannelType.VIDEO.getValue()) {
          userInfo.title = getApplication().getString(R.string.invited_video_title);
        } else {
          userInfo.title = getApplication().getString(R.string.invited_audio_title);
        }
        userInfo.subtitle = getApplication().getString(R.string.invited_subtitle);
        userInfo.accId = callParam.getCallerAccId();
      } else {
        userInfo.nickname = jsonObject.getString(AppParams.CALLED_USER_NAME);
        userInfo.mobile = jsonObject.getString(AppParams.CALLED_USER_MOBILE);
        userInfo.avatar = jsonObject.getString(AppParams.CALLED_USER_AVATAR);
        userInfo.title = getApplication().getString(R.string.connecting);
        userInfo.subtitle = getApplication().getString(R.string.invite_subtitle);
        userInfo.accId = callParam.getCalledAccIdList().get(0);
      }
      this.otherUserInfo = new MutableLiveData<>(userInfo);
      securityTipsModel = new MutableLiveData<>();
      securityAuditManager.startAudit(
          new SecurityAuditManager.SecurityAuditCallback() {
            @Override
            public void callback(SecurityAuditModel model) {
              handleSecurityData(model);
            }
          });
    } catch (JSONException e) {
      e.printStackTrace();
      LogUtil.e(TAG, "json parse error,e:" + e);
    }
  }

  private void handleSecurityData(SecurityAuditModel model) {
    SecurityTipsModel securityTipsModel = new SecurityTipsModel();
    if (model.getType() == SecurityAuditManager.NORMAL_TYPE) {
      CallViewModel.this.securityTipsModel.setValue(securityTipsModel);
      return;
    }
    if (model.getData() != null && model.getData().getAudio() != null) {
      if (selfRtcUid != 0 && selfRtcUid == model.getData().getAudio().getUid()) {
        SecurityFoulUser self = new SecurityFoulUser();
        self.rtcUid = model.getData().getAudio().getUid();
        self.type = SecurityType.AUDIO;
        self.tips = getApplication().getString(R.string.audio_call_security_tips);
        securityTipsModel.self = self;
      } else {
        SecurityFoulUser other = new SecurityFoulUser();
        other.rtcUid = model.getData().getAudio().getUid();
        other.type = SecurityType.AUDIO;
        other.tips = getApplication().getString(R.string.audio_call_other_security_tips);
        securityTipsModel.other = other;
      }
    }

    if (model.getData() != null && model.getData().getVideo() != null) {
      if (selfRtcUid != 0 && selfRtcUid == model.getData().getVideo().getUid()) {
        if (securityTipsModel.self == null) {
          SecurityFoulUser self = new SecurityFoulUser();
          self.rtcUid = model.getData().getVideo().getUid();
          self.type = SecurityType.VIDEO;
          self.tips = getApplication().getString(R.string.video_call_mine_security_tips);
          securityTipsModel.self = self;
        } else {
          securityTipsModel.self.type = SecurityType.ALL;
          securityTipsModel.self.tips =
              getApplication().getString(R.string.video_call_mine_all_security_tips);
        }
      } else {
        if (securityTipsModel.other == null) {
          securityTipsModel.other = new SecurityFoulUser();
          securityTipsModel.other.rtcUid = model.getData().getVideo().getUid();
          securityTipsModel.other.type = SecurityType.VIDEO;
          securityTipsModel.other.tips =
              getApplication().getString(R.string.video_call_other_security_tips);
        } else {
          securityTipsModel.other.type = SecurityType.ALL;
          securityTipsModel.other.tips =
              getApplication().getString(R.string.video_call_other_all_security_tips);
        }
      }
    }
    LogUtil.i(TAG, "securityTipsModel:" + securityTipsModel);
    CallViewModel.this.securityTipsModel.setValue(securityTipsModel);
  }

  public LiveData<OtherUserInfo> getOtherInfo() {
    return otherUserInfo;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    cancelInTheCallTimer();
    NERTCVideoCall.sharedInstance().removeDelegate(neRtcCallDelegate);
    NERtcCallbackProxyMgr.getInstance().removeCallback(rtcCallback);
    securityAuditManager.stopAudit();
    securityAuditManager = null;
  }

  public void startInTheCallTimer() {
    if (inTheCallSecondTimer == null) {
      inTheCallSecondTimer = new SecondsTimer(0, 1000);
    }
    inTheCallSecondTimer.start(
        new Function1<Long, Unit>() {
          @Override
          public Unit invoke(Long aLong) {
            LogUtil.i(TAG, "startInTheCallTimer aLong:" + aLong);
            inTheCallDuration.postValue(aLong);
            return null;
          }
        });
  }

  public void cancelInTheCallTimer() {
    if (inTheCallSecondTimer != null) {
      inTheCallSecondTimer.cancel();
      inTheCallSecondTimer = null;
    }
  }
}
