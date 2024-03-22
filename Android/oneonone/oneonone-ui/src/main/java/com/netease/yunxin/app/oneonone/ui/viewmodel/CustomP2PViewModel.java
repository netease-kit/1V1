// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.event.EventSubscribeService;
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver;
import com.netease.nimlib.sdk.event.model.Event;
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest;
import com.netease.nimlib.sdk.event.model.NimEventType;
import com.netease.nimlib.sdk.event.model.NimOnlineStateEvent;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.app.oneonone.ui.model.YidunAntiSpamResModel;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUIConfigManager;
import com.netease.yunxin.app.oneonone.ui.utils.ChatUtil;
import com.netease.yunxin.app.oneonone.ui.utils.UserInfoUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallEndInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegate;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegateAbs;
import com.netease.yunxin.kit.call.p2p.model.NEHangupReasonCode;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.SettingRepo;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.nertc.nertcvideocall.utils.GsonUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomP2PViewModel extends AndroidViewModel {
  private static final String TAG = "CustomP2PViewModel";
  private String sessionId;
  private static final String ONLINE = "online";
  private static final int EXPIRY = 30 * 24 * 60 * 60;
  private final MutableLiveData<Boolean> onlineStatusData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> typeStateLiveData = new MutableLiveData<>();
  private final MutableLiveData<UserInfo> userInfoLiveData = new MutableLiveData<>();
  private final MutableLiveData<GiftAttachment> giftMessageLiveData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> busyLiveData = new MutableLiveData<>();
  private UserInfo userInfo;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private static final String TYPE_STATE = "typing";
  private static final int DELAY_TIME = 300;
  private ChatUIConfigManager chatUIConfigManager;
  private final EventObserver<List<IMMessageInfo>> incomingMessageObserver =
      new EventObserver<List<IMMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageInfo> event) {
          if (event != null && !event.isEmpty()) {
            for (IMMessageInfo messageInfo : event) {
              // 消息发送者是当前会话对象
              if (ChatUtil.isCurrentSessionMessage(messageInfo, sessionId)) {
                if (ChatUtil.isGiftMessageType(messageInfo)) {
                  giftMessageLiveData.setValue(
                      (GiftAttachment) messageInfo.getMessage().getAttachment());
                }
              }
            }
          }
        }
      };
  private final Observer<List<NimUserInfo>> userInfoUpdateObserver =
      new Observer<List<NimUserInfo>>() {
        @Override
        public void onEvent(List<NimUserInfo> nimUserInfos) {
          for (NimUserInfo nimUserInfo : nimUserInfos) {
            if (TextUtils.equals(nimUserInfo.getAccount(), sessionId)) {
              if (userInfo != null) {
                userInfo.setName(nimUserInfo.getName());
                userInfoLiveData.setValue(userInfo);
              } else {
                fetchTargetUserName();
              }
              break;
            }
          }
        }
      };
  private final Observer<CustomNotification> customNotificationObserver =
      notification -> {
        ALog.d(
            LIB_TAG,
            TAG,
            "mcustomNotificationObserver:"
                + (notification == null ? "null" : notification.getTime()));
        if (!sessionId.equals(notification.getSessionId())
            || notification.getSessionType() != SessionTypeEnum.P2P) {
          return;
        }
        String content = notification.getContent();
        try {
          JSONObject json = new JSONObject(content);
          int id = json.getInt(TYPE_STATE);
          if (id == 1) {
            typeStateLiveData.postValue(true);
          } else {
            typeStateLiveData.postValue(false);
          }
        } catch (JSONException e) {
          ALog.e(TAG, e.getMessage());
        }
      };
  private final EventObserver<IMMessageInfo> msgObserver =
      new EventObserver<IMMessageInfo>() {
        @Override
        public void onEvent(@Nullable IMMessageInfo event) {
          handleRiskMessage(event);
        }
      };

  private final NECallEngineDelegate callEngineDelegate =
      new NECallEngineDelegateAbs() {
        @Override
        public void onCallEnd(NECallEndInfo info) {
          super.onCallEnd(info);
          if (info.reasonCode == NEHangupReasonCode.BUSY) {
            busyLiveData.setValue(true);
          }
        }
      };

  public CustomP2PViewModel(@NonNull Application application) {
    super(application);
  }

  public void initialize(
      String sessionId, UserInfo userInfo, ChatUIConfigManager chatUIConfigManager) {
    this.sessionId = sessionId;
    this.userInfo = userInfo;
    this.chatUIConfigManager = chatUIConfigManager;
    listenOnlineEvent(true);
    listenMessageStatusEvent(true);
    NIMClient.getService(UserServiceObserve.class)
        .observeUserInfoUpdate(userInfoUpdateObserver, true);
    ChatObserverRepo.registerCustomNotificationObserve(customNotificationObserver);
    NECallEngine.sharedInstance().addCallDelegate(callEngineDelegate);
    fetchTargetUserName();
    getTargetUserInfo(sessionId);
    // 语音消息改为扬声器播放
    SettingRepo.setHandsetMode(false);
  }

  private void fetchTargetUserName() {
    ChatRepo.fetchUserInfo(
        sessionId,
        new FetchCallback<UserInfo>() {
          public void onSuccess(@Nullable UserInfo param) {
            ALog.i(TAG, "fetchUserInfo success param:" + param);
            userInfo = param;
            if (userInfo != null) {
              userInfoLiveData.setValue(userInfo);
            }
          }

          public void onFailed(int code) {
            ALog.e(TAG, "fetchUserInfo onFailed code:" + code);
          }

          public void onException(@Nullable Throwable exception) {
            ALog.e(TAG, "fetchUserInfo onException exception:" + exception);
          }
        });
  }

  private void getTargetUserInfo(String sessionId) {
    HttpService.getInstance()
        .getUserInfo(
            sessionId,
            new Callback<ModelResponse<User>>() {
              @Override
              public void onResponse(
                  Call<ModelResponse<User>> call, Response<ModelResponse<User>> response) {
                if (response.body() == null) {
                  return;
                }
                User data = response.body().data;
                ALog.i(TAG, "data:" + data);
                if (data != null) {
                  userInfo = UserInfoUtil.generateUserInfo(data);
                  chatUIConfigManager.setUserInfo(userInfo);
                  getUserState(userInfo.getMobile());
                  sendAccostMessage(userInfo);
                }
              }

              @Override
              public void onFailure(Call<ModelResponse<User>> call, Throwable t) {
                ALog.e(TAG, "getUserInfo failed,exception::" + t);
              }
            });
  }

  private void getUserState(String mobile) {
    HttpService.getInstance()
        .getUserState(
            mobile,
            new Callback<ModelResponse<String>>() {
              @Override
              public void onResponse(
                  Call<ModelResponse<String>> call, Response<ModelResponse<String>> response) {
                if (response.body() != null && response.body().code == 200) {
                  onlineStatusData.setValue(ONLINE.equals(response.body().data));
                }
              }

              @Override
              public void onFailure(Call<ModelResponse<String>> call, Throwable t) {
                ALog.e(TAG, "getUserState failed,exception:" + t);
              }
            });
  }

  private void sendAccostMessage(UserInfo userInfo) {
    String key =
        new StringBuilder()
            .append("insert")
            .append(",")
            .append(sessionId)
            .append(",")
            .append(UserInfoManager.getSelfUserUuid())
            .toString();
    boolean hasInsert = SPUtils.getInstance().getBoolean(key, false);
    if (hasInsert || ChatUtil.isSystemAccount(userInfo.getAccount())) {
      return;
    }
    // 其他任何时间进来列表，看有没有发过"缘分妙不可言"，没发过则发送   记个sp   缘分妙不可言是一条本地消息
    mainHandler.postDelayed(
        () -> {
          ChatUtil.insertAccostMessage(sessionId);
          int result = new Random().nextInt(2);
          if (result == 0) {
            ChatUtil.insertTryAudioCallMessage(userInfo);
          } else {
            ChatUtil.insertTryVideoCallMessage(userInfo);
          }
          SPUtils.getInstance().put(key, true);
        },
        500);
  }

  public MutableLiveData<Boolean> getOnlineStatusData() {
    return onlineStatusData;
  }

  public MutableLiveData<Boolean> getTypeStateLiveData() {
    return typeStateLiveData;
  }

  public MutableLiveData<UserInfo> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public MutableLiveData<GiftAttachment> getGiftMessageLiveData() {
    return giftMessageLiveData;
  }

  public MutableLiveData<Boolean> getBusyLiveData() {
    return busyLiveData;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    listenOnlineEvent(false);
    listenMessageStatusEvent(false);
    NIMClient.getService(UserServiceObserve.class)
        .observeUserInfoUpdate(userInfoUpdateObserver, false);
    ChatObserverRepo.unregisterCustomNotificationObserve(customNotificationObserver);
    registerReceiveMessageObserve(false);
    NECallEngine.sharedInstance().removeCallDelegate(callEngineDelegate);
  }

  private void listenOnlineEvent(boolean listen) {
    EventSubscribeRequest eventSubscribeRequest = new EventSubscribeRequest();
    eventSubscribeRequest.setEventType(NimEventType.ONLINE_STATE.getValue());
    eventSubscribeRequest.setExpiry(EXPIRY);
    ArrayList<String> publishers = new ArrayList<>();
    publishers.add(sessionId);
    eventSubscribeRequest.setPublishers(publishers);
    if (listen) {
      NIMClient.getService(EventSubscribeService.class)
          .subscribeEvent(eventSubscribeRequest)
          .setCallback(
              new RequestCallbackWrapper<List<String>>() {
                @Override
                public void onResult(int code, List<String> result, Throwable exception) {
                  if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && !result.isEmpty()) {
                      for (String s : result) {
                        ALog.i(TAG, "subscribeEvent,accId:" + s);
                      }
                    }
                  }
                }
              });
    } else {
      NIMClient.getService(EventSubscribeService.class)
          .unSubscribeEvent(eventSubscribeRequest)
          .setCallback(
              new RequestCallbackWrapper<List<String>>() {
                @Override
                public void onResult(int code, List<String> result, Throwable exception) {
                  if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && !result.isEmpty()) {
                      for (String s : result) {
                        ALog.i(TAG, "unSubscribeEvent,accId:" + s);
                      }
                    }
                  }
                }
              });
    }
    NIMClient.getService(EventSubscribeServiceObserver.class)
        .observeEventChanged(
            (Observer<List<Event>>)
                events -> {
                  ALog.i(TAG, "events:" + events);
                  if (!events.isEmpty()) {
                    for (Event event : events) {
                      if (event.getEventType() == NimEventType.ONLINE_STATE.getValue()) {
                        if (event.getEventValue()
                            == NimOnlineStateEvent.OnlineStateEventValue.Login.getValue()) {
                          onlineStatusData.setValue(true);
                        } else {
                          onlineStatusData.setValue(false);
                        }
                      }
                    }
                  }
                },
            listen);
  }

  private void listenMessageStatusEvent(boolean listen) {
    // 监听消息状态变化
    if (listen) {
      ChatObserverRepo.registerMsgStatusObserve(msgObserver);
    } else {
      ChatObserverRepo.unregisterMsgStatusObserve(msgObserver);
    }
  }

  private void handleRiskMessage(IMMessageInfo event) {
    if (event != null && !TextUtils.isEmpty(event.getMessage().getYidunAntiSpamRes())) {
      boolean isHitCustomWords = false;
      if (event.getMessage().getMsgType() == MsgTypeEnum.text) {
        String yidunAntiSpamRes = event.getMessage().getYidunAntiSpamRes();
        try {
          JSONObject jsonObject = new JSONObject(yidunAntiSpamRes);
          String ext = jsonObject.optString("ext");
          ALog.i(TAG, "ext:" + ext);
          YidunAntiSpamResModel.ExtBean extBean =
              GsonUtils.fromJson(ext, YidunAntiSpamResModel.ExtBean.class);
          if (extBean != null
              && extBean.getAntispam() != null
              && extBean.getAntispam().getLabels() != null
              && !extBean.getAntispam().getLabels().isEmpty()) {
            for (YidunAntiSpamResModel.ExtBean.AntispamBean.LabelsBean label :
                extBean.getAntispam().getLabels()) {
              List<YidunAntiSpamResModel.ExtBean.AntispamBean.LabelsBean.SubLabelsBean> subLabels =
                  label.getSubLabels();
              if (subLabels != null && !subLabels.isEmpty()) {
                for (YidunAntiSpamResModel.ExtBean.AntispamBean.LabelsBean.SubLabelsBean subLabel :
                    subLabels) {
                  if (subLabel.getDetails() != null
                      && subLabel.getDetails().getKeywords() != null
                      && !subLabel.getDetails().getKeywords().isEmpty()) {
                    List<
                            YidunAntiSpamResModel.ExtBean.AntispamBean.LabelsBean.SubLabelsBean
                                .DetailsBean.KeywordsBean>
                        keywords = subLabel.getDetails().getKeywords();
                    for (YidunAntiSpamResModel.ExtBean.AntispamBean.LabelsBean.SubLabelsBean
                            .DetailsBean.KeywordsBean
                        keyword : keywords) {
                      if (keyword != null && !TextUtils.isEmpty(keyword.getWord())) {
                        isHitCustomWords = true;
                        break;
                      }
                    }
                  }
                }
              }
            }
          }

        } catch (JSONException e) {
          e.printStackTrace();
          ALog.e(TAG, "e:" + e);
        }
        if (isHitCustomWords) {
          mainHandler.postDelayed(() -> ChatUtil.insertPrivacyRiskMessage(sessionId), DELAY_TIME);
        } else {
          mainHandler.postDelayed(() -> ChatUtil.insertCommonRiskMessage(sessionId), DELAY_TIME);
        }
      } else {
        mainHandler.postDelayed(() -> ChatUtil.insertCommonRiskMessage(sessionId), DELAY_TIME);
      }
    }
  }

  public void registerReceiveMessageObserve(boolean register) {
    if (register) {
      ChatObserverRepo.registerReceiveMessageObserve(incomingMessageObserver);
    } else {
      ChatObserverRepo.unregisterReceiveMessageObserve(incomingMessageObserver);
    }
  }
}
