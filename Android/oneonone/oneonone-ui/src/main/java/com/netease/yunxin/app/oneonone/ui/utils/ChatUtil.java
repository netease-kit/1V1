// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageConfig;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.app.oneonone.ui.constant.Constants;
import com.netease.yunxin.app.oneonone.ui.custommessage.AccostMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.CommonRiskAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.OneOnOneChatCustomMessageType;
import com.netease.yunxin.app.oneonone.ui.custommessage.PrivacyRiskAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryAudioCallMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryVideoCallMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import java.io.File;

public class ChatUtil {
  private static final long AUDIO_MESSAGE_MIN_LENGTH = 1000;

  public static void sendTextMessage(
      String accId, String text, ProgressFetchCallback<V2NIMSendMessageResult> callback) {
    V2NIMMessage textMsg = V2NIMMessageCreator.createTextMessage(text);

    V2NIMMessageConfig messageConfig =
        V2NIMMessageConfig.V2NIMMessageConfigBuilder.builder().withReadReceiptEnabled(true).build();
    V2NIMSendMessageParams messageParams =
        V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder()
            .withMessageConfig(messageConfig)
            .build();
    ChatRepo.sendMessage(
        textMsg, V2NIMConversationIdUtil.p2pConversationId(accId), messageParams, callback);
  }

  public static void insertLocalMessage(
      String accId,
      String
          rawAttachment) { //创建IMMessage，其中sessionId代表会话ID，sessionType会话类型，content代表消息内容，customerAttachment自定义消息
    V2NIMMessage customMessage = V2NIMMessageCreator.createCustomMessage("", rawAttachment);
    NIMClient.getService(V2NIMMessageService.class)
        .insertMessageToLocal(
            customMessage,
            V2NIMConversationIdUtil.p2pConversationId(accId),
            NIMClient.getCurrentAccount(),
            System.currentTimeMillis(),
            null,
            null);
  }

  public static void insertAccostMessage(String accId) {
    AccostMessageAttachment customerAttachment = new AccostMessageAttachment();
    ChatUtil.insertLocalMessage(accId, customerAttachment.toJsonStr());
  }

  public static void insertTryAudioCallMessage(UserModel userInfo) {
    TryAudioCallMessageAttachment tryAudioCallMessageAttachment =
        new TryAudioCallMessageAttachment(userInfo);
    ChatUtil.insertLocalMessage(userInfo.account, tryAudioCallMessageAttachment.toJsonStr());
  }

  public static void insertTryVideoCallMessage(UserModel userInfo) {
    TryVideoCallMessageAttachment tryVideoCallMessageAttachment =
        new TryVideoCallMessageAttachment(userInfo);
    ChatUtil.insertLocalMessage(userInfo.account, tryVideoCallMessageAttachment.toJsonStr());
  }

  public static void insertPrivacyRiskMessage(String accid) {
    PrivacyRiskAttachment customerAttachment = new PrivacyRiskAttachment();
    ChatUtil.insertLocalMessage(accid, customerAttachment.toJsonStr());
  }

  public static void insertCommonRiskMessage(String accId) {
    CommonRiskAttachment customerAttachment = new CommonRiskAttachment();
    ChatUtil.insertLocalMessage(accId, customerAttachment.toJsonStr());
  }

  public static void sendAudioMessage(
      String accId,
      File audioFile,
      int audioLength,
      FetchCallback<V2NIMSendMessageResult> callback) {
    if (audioLength < AUDIO_MESSAGE_MIN_LENGTH) {
      ToastX.showShortToast(com.netease.yunxin.kit.chatkit.ui.R.string.chat_message_audio_to_short);
      return;
    }
    V2NIMMessage audioMessage =
        V2NIMMessageCreator.createAudioMessage(audioFile.getAbsolutePath(), "", "", audioLength);
    V2NIMMessageConfig messageConfig =
        V2NIMMessageConfig.V2NIMMessageConfigBuilder.builder().withReadReceiptEnabled(true).build();
    V2NIMSendMessageParams messageParams =
        V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder()
            .withMessageConfig(messageConfig)
            .build();
    ChatRepo.sendMessage(
        audioMessage,
        V2NIMConversationIdUtil.p2pConversationId(accId),
        messageParams,
        new ProgressFetchCallback<V2NIMSendMessageResult>() {
          @Override
          public void onError(int i, @NonNull String s) {
            if (callback != null) {
              callback.onError(i, s);
            }
          }

          @Override
          public void onSuccess(@Nullable V2NIMSendMessageResult v2NIMSendMessageResult) {
            if (callback != null) {
              callback.onSuccess(v2NIMSendMessageResult);
            }
          }

          @Override
          public void onProgress(int i) {}
        });
  }

  public static int getCustomMsgType(IMMessageInfo messageInfo) {
    if (messageInfo != null) {
      if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
        CustomAttachment attachment = (CustomAttachment) messageInfo.getAttachment();
        if (attachment != null) {
          return attachment.getCustomType();
        }
      }
    }
    return -1;
  }

  public static boolean isCurrentSessionMessage(V2NIMMessage messageInfo, String sessionId) {
    return messageInfo != null && TextUtils.equals(messageInfo.getConversationId(), sessionId);
  }

  public static boolean isGiftMessageType(IMMessageInfo messageInfo) {
    return ChatUtil.getCustomMsgType(messageInfo) == OneOnOneChatCustomMessageType.SEND_GIFT_TYPE
        && messageInfo.getAttachment() instanceof GiftAttachment;
  }

  public static boolean isSystemAccount(String account) {
    return Constants.ASSIST_ACCOUNT.equals(account);
  }

  public static boolean isVirtualManSession(String sessionId) {
    return sessionId != null && sessionId.contains("virtually_user");
  }
}
