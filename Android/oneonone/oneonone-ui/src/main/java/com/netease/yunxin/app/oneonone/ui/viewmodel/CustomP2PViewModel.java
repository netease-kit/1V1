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
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.notification.V2NIMBroadcastNotification;
import com.netease.nimlib.sdk.v2.notification.V2NIMCustomNotification;
import com.netease.nimlib.sdk.v2.notification.V2NIMNotificationListener;
import com.netease.nimlib.sdk.v2.notification.V2NIMNotificationService;
import com.netease.nimlib.sdk.v2.subscription.V2NIMSubscribeListener;
import com.netease.nimlib.sdk.v2.subscription.V2NIMSubscriptionService;
import com.netease.nimlib.sdk.v2.subscription.enums.V2NIMUserStatusType;
import com.netease.nimlib.sdk.v2.subscription.model.V2NIMUserStatus;
import com.netease.nimlib.sdk.v2.subscription.option.V2NIMSubscribeUserStatusOption;
import com.netease.nimlib.sdk.v2.subscription.option.V2NIMUnsubscribeUserStatusOption;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.nimlib.sdk.v2.user.V2NIMUserListener;
import com.netease.nimlib.sdk.v2.user.V2NIMUserService;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
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
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.nertc.nertcvideocall.utils.GsonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomP2PViewModel extends AndroidViewModel {
  private static final String TAG = "CustomP2PViewModel";
  private String accId;
  private static final String ONLINE = "online";
  private static final int EXPIRY = 30 * 24 * 60 * 60;
  private final MutableLiveData<Boolean> onlineStatusData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> typeStateLiveData = new MutableLiveData<>();
  private final MutableLiveData<UserModel> userInfoLiveData = new MutableLiveData<>();
  private final MutableLiveData<GiftAttachment> giftMessageLiveData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> busyLiveData = new MutableLiveData<>();
  private UserModel userInfo;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private static final String TYPE_STATE = "typing";
  private static final int DELAY_TIME = 300;
  private ChatUIConfigManager chatUIConfigManager;
  private V2NIMMessageListener messageListener =
      new V2NIMMessageListener() {
        @Override
        public void onReceiveMessages(List<V2NIMMessage> messages) {
          if (messages != null) {
            for (V2NIMMessage message : messages) {
              if (message != null) {
                // 消息发送者是当前会话对象
                if (ChatUtil.isCurrentSessionMessage(message, accId)) {
                  IMMessageInfo messageInfo1 = new IMMessageInfo(message);
                  messageInfo1.parseAttachment();
                  if (ChatUtil.isGiftMessageType(messageInfo1)) {
                    giftMessageLiveData.setValue((GiftAttachment) message.getAttachment());
                  }
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
        public void onSendMessage(V2NIMMessage message) {
          handleRiskMessage(message);
        }

        @Override
        public void onReceiveMessagesModified(List<V2NIMMessage> messages) {}
      };

  private final V2NIMSubscribeListener subscribeListener =
      new V2NIMSubscribeListener() {
        @Override
        public void onUserStatusChanged(List<V2NIMUserStatus> userStatusList) {
          ALog.i(TAG, "onUserStatusChanged userStatusList = " + userStatusList);
          if (userStatusList != null) {
            for (V2NIMUserStatus event : userStatusList) {
              if (event.getStatusType()
                  == V2NIMUserStatusType.V2NIM_USER_STATUS_TYPE_LOGIN.getValue()) {
                onlineStatusData.setValue(true);
              } else {
                onlineStatusData.setValue(false);
              }
            }
          }
        }
      };

  private final V2NIMUserListener userInfoUpdateObserver =
      new V2NIMUserListener() {
        @Override
        public void onUserProfileChanged(List<V2NIMUser> users) {

          for (V2NIMUser nimUserInfo : users) {
            if (TextUtils.equals(nimUserInfo.getAccountId(), accId)) {
              if (userInfo != null) {
                userInfo.nickname = (nimUserInfo.getName());
                userInfoLiveData.setValue(userInfo);
              } else {
                fetchTargetUserName();
              }
              break;
            }
          }
        }

        @Override
        public void onBlockListAdded(V2NIMUser user) {}

        @Override
        public void onBlockListRemoved(String accountId) {}
      };

  private final V2NIMNotificationListener customNotificationObserver =
      new V2NIMNotificationListener() {
        @Override
        public void onReceiveCustomNotifications(
            List<V2NIMCustomNotification> customNotifications) {
          for (V2NIMCustomNotification notification : customNotifications) {
            ALog.d(
                LIB_TAG,
                TAG,
                "customNotificationObserver:"
                    + (notification == null ? "null" : notification.getTimestamp()));
            if (notification != null) {
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
            }
          }
        }

        @Override
        public void onReceiveBroadcastNotifications(
            List<V2NIMBroadcastNotification> broadcastNotifications) {}
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
      String accId, UserModel userInfo, ChatUIConfigManager chatUIConfigManager) {
    this.accId = accId;
    this.userInfo = userInfo;
    this.chatUIConfigManager = chatUIConfigManager;
    listenOnlineEvent(true);

    NIMClient.getService(V2NIMUserService.class).addUserListener(userInfoUpdateObserver);
    NIMClient.getService(V2NIMNotificationService.class)
        .addNotificationListener(customNotificationObserver);
    NECallEngine.sharedInstance().addCallDelegate(callEngineDelegate);
    fetchTargetUserName();
    getTargetUserInfo(accId);
    // 语音消息改为扬声器播放
    SettingRepo.setHandsetMode(false);
    IMKitClient.setSendMessageCallback(
        new ProgressFetchCallback<V2NIMSendMessageResult>() {
          @Override
          public void onProgress(int i) {}

          @Override
          public void onSuccess(@Nullable V2NIMSendMessageResult v2NIMSendMessageResult) {
            ALog.e(TAG, "sendMessage onSuccess");
            handleRiskMessage(v2NIMSendMessageResult);
          }

          @Override
          public void onError(int i, @NonNull String s) {
            ALog.e(TAG, "sendMessage onError code:" + i + ",msg:" + s);
          }
        });
  }

  private void fetchTargetUserName() {
    ContactRepo.getUserInfo(
        Collections.singletonList(accId),
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onError(int code, @Nullable String s) {
            ALog.e(TAG, "fetchUserInfo onFailed code:" + code);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMUser> v2NIMUsers) {
            ALog.i(TAG, "fetchUserInfo success v2NIMUsers:" + v2NIMUsers);
            userInfo = new UserModel();
            if (v2NIMUsers != null && v2NIMUsers.get(0) != null) {
              userInfo.nickname = v2NIMUsers.get(0).getName();
              userInfo.account = v2NIMUsers.get(0).getAccountId();
              userInfo.avatar = v2NIMUsers.get(0).getAvatar();
              userInfo.mobile = v2NIMUsers.get(0).getMobile();
            }
            userInfoLiveData.setValue(userInfo);
          }
        });
  }

  private void getTargetUserInfo(String accId) {
    HttpService.getInstance()
        .getUserInfo(
            accId,
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
                  getUserState(userInfo.mobile);
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

  private void sendAccostMessage(UserModel userInfo) {
    String key =
        new StringBuilder()
            .append("insert")
            .append(",")
            .append(accId)
            .append(",")
            .append(UserInfoManager.getSelfUserUuid())
            .toString();
    boolean hasInsert = SPUtils.getInstance().getBoolean(key, false);
    if (hasInsert || ChatUtil.isSystemAccount(userInfo.account)) {
      return;
    }
    // 其他任何时间进来列表，看有没有发过"缘分妙不可言"，没发过则发送   记个sp   缘分妙不可言是一条本地消息
    mainHandler.postDelayed(
        () -> {
          ChatUtil.insertAccostMessage(accId);
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

  public MutableLiveData<UserModel> getUserInfoLiveData() {
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
    NIMClient.getService(V2NIMUserService.class).removeUserListener(userInfoUpdateObserver);
    NIMClient.getService(V2NIMNotificationService.class)
        .removeNotificationListener(customNotificationObserver);
    registerReceiveMessageObserve(false);
    NECallEngine.sharedInstance().removeCallDelegate(callEngineDelegate);
  }

  private void listenOnlineEvent(boolean listen) {
    ArrayList<String> publishers = new ArrayList<>();
    publishers.add(V2NIMConversationIdUtil.conversationTargetId(accId));
    V2NIMSubscribeUserStatusOption option =
        new V2NIMSubscribeUserStatusOption(publishers, EXPIRY, false);

    V2NIMUnsubscribeUserStatusOption option1 = new V2NIMUnsubscribeUserStatusOption();
    option1.setAccountIds(publishers);

    if (listen) {
      NIMClient.getService(V2NIMSubscriptionService.class).subscribeUserStatus(option, null, null);
    } else {
      NIMClient.getService(V2NIMSubscriptionService.class)
          .unsubscribeUserStatus(option1, null, null);
    }

    NIMClient.getService(V2NIMSubscriptionService.class).addSubscribeListener(subscribeListener);
  }

  private void handleRiskMessage(V2NIMSendMessageResult sendMessageResult) {
    if (sendMessageResult != null && sendMessageResult.getAntispamResult() != null) {
      boolean isHitCustomWords = false;
      String yidunAntiSpamRes = sendMessageResult.getAntispamResult();
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
        ALog.e(TAG, "e:" + e);
      }
      if (isHitCustomWords) {
        mainHandler.postDelayed(() -> ChatUtil.insertPrivacyRiskMessage(accId), DELAY_TIME);
      } else {
        mainHandler.postDelayed(() -> ChatUtil.insertCommonRiskMessage(accId), DELAY_TIME);
      }
    }
  }

  private void handleRiskMessage(V2NIMMessage message) {
    if (message != null
        && !TextUtils.isEmpty(message.getAntispamConfig().getAntispamCustomMessage())) {
      boolean isHitCustomWords = false;
      if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
        String yidunAntiSpamRes = message.getAntispamConfig().getAntispamCustomMessage();
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
          ALog.e(TAG, "e:" + e);
        }
        if (isHitCustomWords) {
          mainHandler.postDelayed(() -> ChatUtil.insertPrivacyRiskMessage(accId), DELAY_TIME);
        } else {
          mainHandler.postDelayed(() -> ChatUtil.insertCommonRiskMessage(accId), DELAY_TIME);
        }
      } else {
        mainHandler.postDelayed(() -> ChatUtil.insertCommonRiskMessage(accId), DELAY_TIME);
      }
    }
  }

  public void registerReceiveMessageObserve(boolean register) {
    if (register) {
      NIMClient.getService(V2NIMMessageService.class).addMessageListener(messageListener);
    } else {
      NIMClient.getService(V2NIMMessageService.class).removeMessageListener(messageListener);
    }
  }
}
