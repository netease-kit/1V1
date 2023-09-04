// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.text.TextUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.app.oneonone.ui.constant.Constants;
import com.netease.yunxin.app.oneonone.ui.custommessage.AccostMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.CommonRiskAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.OneOnOneChatCustomMessageType;
import com.netease.yunxin.app.oneonone.ui.custommessage.PrivacyRiskAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryAudioCallMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryVideoCallMessageAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.io.File;

public class ChatUtil {
  private static final long AUDIO_MESSAGE_MIN_LENGTH = 1000;

  public static void sendTextMessage(
      String sessionId,
      SessionTypeEnum sessionType,
      String text,
      boolean resend,
      FetchCallback<Void> callback) {
    IMMessage textMsg = MessageBuilder.createTextMessage(sessionId, sessionType, text);
    textMsg.setMsgAck();
    ChatRepo.sendMessage(textMsg, resend, callback);
  }

  public static void sendCustomMessage(
      String sessionId,
      SessionTypeEnum sessionType,
      String content,
      MsgAttachment attachment,
      FetchCallback<Void>
          callback) { //创建IMMessage，其中sessionId代表会话ID，sessionType会话类型，content代表消息内容，customerAttachment自定义消息
    IMMessage customMessage =
        MessageBuilder.createCustomMessage(sessionId, sessionType, content, attachment);
    //ChaRepo提供的发送消息接口，任何消息类型都可以使用该接口进行发送
    //customMessage 表示创建的消息体。如果需要在消息发送失败后重发，标记resend参数为true，否则填false。callback表示消息发送回调
    ChatRepo.sendMessage(customMessage, false, callback);
  }

  public static void insertLocalMessage(
      String sessionId,
      SessionTypeEnum sessionType,
      String content,
      MsgAttachment attachment,
      FetchCallback<Void>
          callback) { //创建IMMessage，其中sessionId代表会话ID，sessionType会话类型，content代表消息内容，customerAttachment自定义消息
    IMMessage customMessage =
        MessageBuilder.createCustomMessage(sessionId, sessionType, content, attachment);
    NIMClient.getService(MsgService.class)
        .saveMessageToLocal(customMessage, true)
        .setCallback(
            new RequestCallback<Void>() {
              @Override
              public void onSuccess(Void result) {
                if (callback != null) {
                  callback.onSuccess(result);
                }
              }

              @Override
              public void onFailed(int code) {
                if (callback != null) {
                  callback.onFailed(code);
                }
              }

              @Override
              public void onException(Throwable exception) {
                if (callback != null) {
                  callback.onException(exception);
                }
              }
            });
  }

  public static void insertAccostMessage(String sessionId) {
    AccostMessageAttachment customerAttachment = new AccostMessageAttachment();
    ChatUtil.insertLocalMessage(sessionId, SessionTypeEnum.P2P, "", customerAttachment, null);
  }

  public static void insertTryAudioCallMessage(UserInfo userInfo) {
    TryAudioCallMessageAttachment tryAudioCallMessageAttachment =
        new TryAudioCallMessageAttachment(userInfo);
    ChatUtil.insertLocalMessage(
        userInfo.getAccount(), SessionTypeEnum.P2P, "", tryAudioCallMessageAttachment, null);
  }

  public static void insertTryVideoCallMessage(UserInfo userInfo) {
    TryVideoCallMessageAttachment tryVideoCallMessageAttachment =
        new TryVideoCallMessageAttachment(userInfo);
    ChatUtil.insertLocalMessage(
        userInfo.getAccount(), SessionTypeEnum.P2P, "", tryVideoCallMessageAttachment, null);
  }

  public static void insertPrivacyRiskMessage(String sessionId) {
    PrivacyRiskAttachment customerAttachment = new PrivacyRiskAttachment();
    ChatUtil.insertLocalMessage(sessionId, SessionTypeEnum.P2P, "", customerAttachment, null);
  }

  public static void insertCommonRiskMessage(String sessionId) {
    CommonRiskAttachment customerAttachment = new CommonRiskAttachment();
    ChatUtil.insertLocalMessage(sessionId, SessionTypeEnum.P2P, "", customerAttachment, null);
  }

  public static IMMessage queryLastMessage(String account) {
    IMMessage imMessage =
        NIMClient.getService(MsgService.class).queryLastMessage(account, SessionTypeEnum.P2P);
    return imMessage;
  }

  public static void sendAudioMessage(
      String sessionID,
      SessionTypeEnum sessionType,
      File audioFile,
      long audioLength,
      boolean resend,
      FetchCallback<Void> callback) {
    if (audioLength < AUDIO_MESSAGE_MIN_LENGTH) {
      ToastX.showShortToast(com.netease.yunxin.kit.chatkit.ui.R.string.chat_message_audio_to_short);
      return;
    }
    IMMessage audioMessage =
        MessageBuilder.createAudioMessage(sessionID, sessionType, audioFile, audioLength);
    audioMessage.setMsgAck();
    ChatRepo.sendMessage(audioMessage, resend, callback);
  }

  public static int getCustomMsgType(IMMessageInfo messageInfo) {
    if (messageInfo != null) {
      if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.custom) {
        CustomAttachment attachment = (CustomAttachment) messageInfo.getMessage().getAttachment();
        if (attachment != null) {
          return attachment.getType();
        }
      }
    }
    return -1;
  }

  public static boolean isCurrentSessionMessage(IMMessageInfo messageInfo, String sessionId) {
    return messageInfo != null
        && TextUtils.equals(messageInfo.getMessage().getSessionId(), sessionId);
  }

  public static boolean isGiftMessageType(IMMessageInfo messageInfo) {
    return ChatUtil.getCustomMsgType(messageInfo) == OneOnOneChatCustomMessageType.SEND_GIFT_TYPE
        && messageInfo.getMessage().getAttachment() instanceof GiftAttachment;
  }

  public static boolean isSystemAccount(String account) {
    return Constants.ASSIST_ACCOUNT.equals(account);
  }

  public static boolean isVirtualManSession(String sessionId) {
    return sessionId != null && sessionId.contains("virtually_user");
  }
}
