// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.content.Context;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.netease.yunxin.app.oneonone.ui.activity.CustomChatP2PActivity;
import com.netease.yunxin.app.oneonone.ui.custommessage.AccostMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.AccostMessageViewHolder;
import com.netease.yunxin.app.oneonone.ui.custommessage.AssistantAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.AssistantMessageViewHolder;
import com.netease.yunxin.app.oneonone.ui.custommessage.CommonRiskAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.CommonRiskMessageViewHolder;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.GiftMessageViewHolder;
import com.netease.yunxin.app.oneonone.ui.custommessage.OneOnOneChatCustomMessageType;
import com.netease.yunxin.app.oneonone.ui.custommessage.PrivacyRiskAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.PrivacyRiskMessageViewHolder;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryAudioCallMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryAudioCallMessageViewHolder;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryVideoCallMessageAttachment;
import com.netease.yunxin.app.oneonone.ui.custommessage.TryVideoCallMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.locationkit.LocationKitClient;

public class IMUIKitUtil {
  public static void initIMUIKit(Context context, SDKOptions options) {
    IMKitClient.init(context, null, options);
    if (NIMUtil.isMainProcess(context)) {
      LocationKitClient.init();
      registerCustomAttachParse();
    }
    registerPageRouter();
  }

  private static void registerCustomAttachParse() {
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TYPE, AccostMessageAttachment.class);
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE,
        TryAudioCallMessageAttachment.class);
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE,
        TryVideoCallMessageAttachment.class);
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.SEND_GIFT_TYPE, GiftAttachment.class);
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE, PrivacyRiskAttachment.class);
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE, CommonRiskAttachment.class);
    ChatKitClient.addCustomAttach(
        OneOnOneChatCustomMessageType.ASSISTANT_MESSAGE_TYPE, AssistantAttachment.class);

    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TYPE, AccostMessageViewHolder.class);
    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE,
        TryAudioCallMessageViewHolder.class);
    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE,
        TryVideoCallMessageViewHolder.class);
    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.SEND_GIFT_TYPE, GiftMessageViewHolder.class);
    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE,
        PrivacyRiskMessageViewHolder.class);
    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE, CommonRiskMessageViewHolder.class);
    ChatKitClient.addCustomViewHolder(
        OneOnOneChatCustomMessageType.ASSISTANT_MESSAGE_TYPE, AssistantMessageViewHolder.class);
  }

  private static void registerPageRouter() {
    XKitRouter.registerRouter(RouterConstant.PATH_CHAT_P2P_PAGE, CustomChatP2PActivity.class);
  }
}
