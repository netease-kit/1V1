// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.v2.message.V2NIMClearHistoryNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageListener;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePinNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageQuickCommentNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRevokeNotification;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService;
import com.netease.nimlib.sdk.v2.message.V2NIMP2PMessageReadReceipt;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.OtherUserInfo;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUtil;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.SecurityAuditManager;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityAuditModel;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityFoulUser;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityTipsModel;
import com.netease.yunxin.app.oneonone.ui.utils.security.SecurityType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallEndInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegate;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegateAbs;
import com.netease.yunxin.kit.call.p2p.model.NECallInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallTypeChangeInfo;
import com.netease.yunxin.kit.call.p2p.model.NEHangupReasonCode;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.common.network.Response;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackExTemp;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackProxyMgr;
import com.netease.yunxin.nertc.ui.base.AVChatSoundPlayer;
import com.netease.yunxin.nertc.ui.base.CallParam;
import com.netease.yunxin.nertc.ui.p2p.CallUIOperationsMgr;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;

public class CallViewModel extends AndroidViewModel {
  private static final String TAG = "CallViewModel";
  private final MutableLiveData<OtherUserInfo> otherUserInfo = new MutableLiveData<>();
  private SecurityAuditManager securityAuditManager = new SecurityAuditManager();
  private final MutableLiveData<SecurityTipsModel> securityTipsModel = new MutableLiveData<>();;
  private final MutableLiveData<Long> inTheCallDuration = new MutableLiveData<>();
  private long selfRtcUid;
  private final MutableLiveData<Boolean> selfJoinChannelSuccessData = new MutableLiveData<>();
  private OtherUserInfo userInfo;
  private CallParam callParam;
  private final MutableLiveData<Boolean> switchToInTheCall = new MutableLiveData<>();
  private final MutableLiveData<String> toastData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> callFinished = new MutableLiveData<>();
  private final MutableLiveData<AVChatSoundPlayer.RingerTypeEnum> playRing =
      new MutableLiveData<>();
  private final MutableLiveData<Boolean> remoteVideoMute = new MutableLiveData<>();
  private final MutableLiveData<Long> otherRtcUid = new MutableLiveData<>();
  private final MutableLiveData<GiftAttachment> giftAttachmentData = new MutableLiveData<>();
  private final NERtcCallbackExTemp rtcCallback =
      new NERtcCallbackExTemp() {
        @Override
        public void onJoinChannel(int i, long l, long l1, long l2) {
          super.onJoinChannel(i, l, l1, l2);
          selfRtcUid = l2;
          selfJoinChannelSuccessData.postValue(true);
          doConfigSpeaker(callParam.getCallType() != ChannelType.AUDIO.getValue());
          reportOneOnOneRtcRoomCreate(l);
        }

        @Override
        public void onUserJoined(long l) {
          super.onUserJoined(l);
          otherRtcUid.postValue(l);
        }
      };

  private final NECallEngineDelegate neCallEngineDelegate =
      new NECallEngineDelegateAbs() {
        @Override
        public void onCallConnected(NECallInfo info) {
          super.onCallConnected(info);
          LogUtil.i(TAG, "onUserEnter,userId:" + info.otherUserInfo().accId);
          switchToInTheCall.postValue(true);
          // 对方断网重连会重新触发onUserEnter回调
          //          startInTheCallTimer();
        }

        @Override
        public void onCallTypeChange(NECallTypeChangeInfo info) {
          super.onCallTypeChange(info);
        }

        @Override
        public void onCallEnd(NECallEndInfo info) {
          super.onCallEnd(info);
          if (info.reasonCode == NEHangupReasonCode.CALLEE_CANCELED) {
            if (callParam.isCalled()) {
              toastData.postValue(getApplication().getString(R.string.cancel_by_other));
            }
          } else if (info.reasonCode == NEHangupReasonCode.CALLER_REJECTED) {
            ALog.i(
                TAG,
                "reject , hangup extraString"
                    + info.extraString); // NECallEngine.sharedInstance().hangup 携带的信息
            if (!callParam.isCalled()) {
              if (!TextUtils.isEmpty(info.extraString)) {
                ALog.i(TAG, "show busy dialog");
              } else {
                toastData.postValue(getApplication().getString(R.string.reject_tips));
                playRing.postValue(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);
              }
            }
          } else if (info.reasonCode == NEHangupReasonCode.TIME_OUT) {
            if (callParam.isCalled()) {
              toastData.postValue(getApplication().getString(R.string.called_timeout_tips));
            } else {
              toastData.postValue(getApplication().getString(R.string.caller_timeout_tips));
            }
          } else if (info.reasonCode == NEHangupReasonCode.BUSY) {
            if (!callParam.isCalled()) {
              playRing.postValue(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
            }
          } else if (info.reasonCode == NEHangupReasonCode.USER_RTC_LEAVE) {
            toastData.setValue(getApplication().getString(R.string.other_end_call));
            callFinished.postValue(true);
          } else if (info.reasonCode == NEHangupReasonCode.BE_HUNG_UP) {
            toastData.setValue(getApplication().getString(R.string.other_end_call));
            callFinished.postValue(true);
          }

          callFinished.postValue(true);
        }

        @Override
        public void onVideoMuted(String userId, boolean mute) {
          super.onVideoMuted(userId, mute);
          remoteVideoMute.postValue(mute);
        }

        @Override
        public void onAudioMuted(String userId, boolean mute) {
          super.onAudioMuted(userId, mute);
        }
      };

  private final V2NIMMessageListener messageListener =
      new V2NIMMessageListener() {
        @Override
        public void onReceiveMessages(List<V2NIMMessage> messages) {
          if (messages != null) {
            for (V2NIMMessage messageInfo : messages) {
              if (ChatUtil.isCurrentSessionMessage(messageInfo, userInfo.accId)) {
                IMMessageInfo messageInfo1 = new IMMessageInfo(messageInfo);
                messageInfo1.parseAttachment();
                if (ChatUtil.isGiftMessageType(messageInfo1)) {
                  handleGiftMessage((GiftAttachment) messageInfo.getAttachment());
                }
              }
            }
          }
        }

        @Override
        public void onReceiveP2PMessageReadReceipts(
            List<V2NIMP2PMessageReadReceipt> readReceipts) {}

        @Override
        public void onReceiveTeamMessageReadReceipts(
            List<V2NIMTeamMessageReadReceipt> readReceipts) {}

        @Override
        public void onMessageRevokeNotifications(
            List<V2NIMMessageRevokeNotification> revokeNotifications) {}

        @Override
        public void onMessagePinNotification(V2NIMMessagePinNotification pinNotification) {}

        @Override
        public void onMessageQuickCommentNotification(
            V2NIMMessageQuickCommentNotification quickCommentNotification) {}

        @Override
        public void onMessageDeletedNotifications(
            List<V2NIMMessageDeletedNotification> messageDeletedNotifications) {}

        @Override
        public void onClearHistoryNotifications(
            List<V2NIMClearHistoryNotification> clearHistoryNotifications) {}

        @Override
        public void onSendMessage(V2NIMMessage message) {}

        @Override
        public void onReceiveMessagesModified(List<V2NIMMessage> messages) {}
      };

  private void handleGiftMessage(GiftAttachment giftAttachment) {
    giftAttachmentData.postValue(giftAttachment);
  }

  public CallViewModel(@NonNull Application application) {
    super(application);
    NECallEngine.sharedInstance().addCallDelegate(neCallEngineDelegate);
    NERtcCallbackProxyMgr.getInstance().addCallback(rtcCallback);
    NIMClient.getService(V2NIMMessageService.class).addMessageListener(messageListener);
    CallUIOperationsMgr.INSTANCE.configTimeTick(
        new CallUIOperationsMgr.TimeTickConfig(
            new Function1<Long, Unit>() {
              @Override
              public Unit invoke(Long aLong) {
                ALog.i(TAG, "CallUIOperationsMgr aLong:" + aLong);
                inTheCallDuration.postValue(aLong);
                return null;
              }
            },
            1000,
            0));
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

  public MutableLiveData<GiftAttachment> getGiftAttachment() {
    return giftAttachmentData;
  }

  public MutableLiveData<Boolean> getRemoteVideoMute() {
    return remoteVideoMute;
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
      userInfo.callType = callParam.getCallType();
      if (callParam.isCalled()) {
        userInfo.nickname = jsonObject.optString(AppParams.CALLER_USER_NAME);
        userInfo.avatar = jsonObject.optString(AppParams.CALLER_USER_AVATAR);
        if (callParam.getCallType() == ChannelType.VIDEO.getValue()) {
          userInfo.title = getApplication().getString(R.string.invited_video_title);
        } else {
          userInfo.title = getApplication().getString(R.string.invited_audio_title);
        }
        userInfo.subtitle = getApplication().getString(R.string.invited_subtitle);
        userInfo.accId = NIMClient.getCurrentAccount();
      } else {
        userInfo.nickname = jsonObject.optString(AppParams.CALLED_USER_NAME);
        userInfo.mobile = jsonObject.optString(AppParams.CALLED_USER_MOBILE);
        userInfo.avatar = jsonObject.optString(AppParams.CALLED_USER_AVATAR);
        userInfo.title = getApplication().getString(R.string.connecting);
        userInfo.subtitle = getApplication().getString(R.string.invite_subtitle);
        userInfo.accId = callParam.getCalledAccId();
      }
    } catch (JSONException e) {
      LogUtil.e(TAG, "json parse error,e:" + e);
    }
    otherUserInfo.setValue(userInfo);
    securityAuditManager.startAudit(
        new SecurityAuditManager.SecurityAuditCallback() {
          @Override
          public void callback(SecurityAuditModel model) {
            handleSecurityData(model);
          }
        });
  }

  private void handleSecurityData(SecurityAuditModel model) {
    SecurityTipsModel securityTipsModel = new SecurityTipsModel();
    if (model.getType() == SecurityAuditManager.NORMAL_TYPE) {
      LogUtil.i("SecurityAuditManager", "securityTipsModel:" + securityTipsModel);
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
    LogUtil.i("SecurityAuditManager", "securityTipsModel:" + securityTipsModel);
    CallViewModel.this.securityTipsModel.setValue(securityTipsModel);
  }

  public LiveData<OtherUserInfo> getOtherInfo() {
    return otherUserInfo;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    NECallEngine.sharedInstance().removeCallDelegate(neCallEngineDelegate);
    NERtcCallbackProxyMgr.getInstance().removeCallback(rtcCallback);
    NIMClient.getService(V2NIMMessageService.class).removeMessageListener(messageListener);
    securityAuditManager.stopAudit();
    securityAuditManager = null;
    ALog.i(TAG, "onCleared");
  }

  private void reportOneOnOneRtcRoomCreate(long rtcChannelId) {
    HttpService.getInstance()
        .reportRtcRoom(
            rtcChannelId,
            new Callback<ModelResponse<Response>>() {
              @Override
              public void onResponse(
                  Call<ModelResponse<Response>> call,
                  retrofit2.Response<ModelResponse<Response>> response) {}

              @Override
              public void onFailure(Call<ModelResponse<Response>> call, Throwable t) {}
            });
  }

  public MutableLiveData<Boolean> getSelfJoinChannelSuccessData() {
    return selfJoinChannelSuccessData;
  }

  public boolean isMuteLocalAudio() {
    return CallUIOperationsMgr.INSTANCE.getCallInfoWithUIState().isLocalMuteAudio();
  }

  public boolean isLocalCameraIsOpen() {
    return !CallUIOperationsMgr.INSTANCE.getCallInfoWithUIState().isLocalMuteVideo();
  }

  public boolean isRemoteCameraIsOpen() {
    return !CallUIOperationsMgr.INSTANCE.getCallInfoWithUIState().isRemoteMuteVideo();
  }

  public boolean isSelfInSmallUi() {
    return CallUIOperationsMgr.INSTANCE.getCallInfoWithUIState().isLocalSmallVideo();
  }

  public void doConfigSpeaker(boolean enableSpeaker) {
    CallUIOperationsMgr.INSTANCE.doConfigSpeaker(enableSpeaker);
  }

  public boolean isSpeakerOn() {
    return CallUIOperationsMgr.INSTANCE.isSpeakerOn();
  }

  public void doMuteAudio(boolean mute) {
    CallUIOperationsMgr.INSTANCE.doMuteAudio(mute);
  }

  public void doMuteVideo(boolean mute) {
    CallUIOperationsMgr.INSTANCE.doMuteVideo(mute);
  }

  public void updateSelfInSmallFlag(boolean isSelfInSmallUi) {
    CallUIOperationsMgr.CallInfoWithUIState callInfoWithUIState =
        CallUIOperationsMgr.INSTANCE.getCallInfoWithUIState();
    CallUIOperationsMgr.INSTANCE.updateUIState(
        callInfoWithUIState.isRemoteMuteVideo(),
        callInfoWithUIState.isLocalMuteVideo(),
        callInfoWithUIState.isLocalMuteAudio(),
        callInfoWithUIState.isLocalMuteSpeaker(),
        callInfoWithUIState.getCameraDeviceStatus(),
        isSelfInSmallUi,
        callInfoWithUIState.isVirtualBlur());
  }
}
