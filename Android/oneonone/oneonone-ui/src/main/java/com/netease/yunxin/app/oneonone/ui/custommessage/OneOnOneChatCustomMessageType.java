// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.custommessage;

public class OneOnOneChatCustomMessageType {
  // IM UIKit自定义消息类型推荐从1000开始
  /** 第一条消息发送后的自定义消息"缘分妙不可言，快来聊聊吧" */
  public static final int ACCOST_MESSAGE_TYPE = 1001;
  /** 试试语音通话，体验TA的声音 */
  public static final int TRY_AUDIO_CALL_MESSAGE_TYPE = 1002;
  /** 试试视频通话，体验TA的声音 */
  public static final int TRY_VIDEO_CALL_MESSAGE_TYPE = 1003;
  /** 发送给对方、送你礼物 根据自定义消息的内部参数 "senderAccount" "giftCount" "giftType" */
  public static final int SEND_GIFT_TYPE = 1004;
  /** ⚠️ 注意保护个人隐私安全 ⚠️ 请勿提供第三方联系方式 */
  public static final int PRIVACY_RISK_MESSAGE_TYPE = 1005;
  /** 通用违规消息 (图片、视频) */
  public static final int COMMON_RISK_MESSAGE_TYPE = 1006;
  /** 小秘书消息 */
  public static final int ASSISTANT_MESSAGE_TYPE = 1007;
}
