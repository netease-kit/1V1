// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils.callkit;

import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.model.V2NIMMessageCallDuration;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.alog.ParameterMap;
import com.netease.yunxin.kit.call.p2p.model.NERecord;
import com.netease.yunxin.kit.call.p2p.model.NERecordProvider;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import java.util.Collections;

public class CustomCallOrderProvider implements NERecordProvider {
  private static final String TAG = "CustomCallOrderProvider";

  @Override
  public void onRecordSend(NERecord neRecord) {
    sendOrder(neRecord.callType, neRecord.accId, neRecord.callState);
  }

  public static void sendOrder(int callType, String accountId, int status) {
    ALog.dApi(
        TAG,
        new ParameterMap("sendOrder")
            .append("status", status)
            .append("callType", callType)
            .append("accountId", accountId)
            .toValue());
    V2NIMMessageCallDuration durations = new V2NIMMessageCallDuration(accountId, 0);
    V2NIMMessage message =
        V2NIMMessageCreator.createCallMessage(
            callType, "", status, Collections.singletonList(durations), null);
    ChatRepo.sendMessage(message, V2NIMConversationIdUtil.p2pConversationId(accountId), null, null);
  }
}
