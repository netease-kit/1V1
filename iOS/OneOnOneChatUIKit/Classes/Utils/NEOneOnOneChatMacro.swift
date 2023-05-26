// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

// func NEOneOnOneChatLocalized(_ key: String) -> String? {
////  NEOneOnOneChatUtil.ne_localized(key)
//    ne_localized(<#T##key: String##String#>)
// }
//
// func NEOneOnOneChatImageNamed(_ imageName: String) -> UIImage? {
//  NEOneOnOneChatUtil.ne_chatUI_imageName(imageName: imageName)
// }

/// 音频呼叫通知
let AudioCallAction = "AudioCallAction"
/// 视频呼叫通知
let VideoCallAction = "VideoCallAction"

public enum OneOnOneChatCustomMessageType {
  // IM UIKit自定义消息类型推荐从1000开始
  /**
   第一条消息发送后的自定义消息"缘分妙不可言，快来聊聊吧"
   */
  public static var ACCOST_MESSAGE_TIPS_TYPE: Int = 1001
  /**
   试试语音通话，体验TA的声音
   */
  public static var TRY_AUDIO_CALL_MESSAGE_TYPE: Int = 1002
  /**
   试试视频通话，体验TA的声音
   */
  public static var TRY_VIDEO_CALL_MESSAGE_TYPE: Int = 1003
  /**
   礼物消息
   */
  public static var SEND_GIFT_TYPE: Int = 1004
  /**
   ⚠️ 注意保护个人隐私安全 ⚠️ 请勿提供第三方联系方式
   */
  public static var PRIVACY_RISK_MESSAGE_TYPE: Int = 1005
  /**
   *  通用违规消息 (图片、视频)
   */
  public static var COMMON_RISK_MESSAGE_TYPE: Int = 1006
  /**
   小秘书礼物消息
   */
  public static var OFFICIAL_GIFT_TYPE: Int = 1007
}

// 对应SEND_GIFT_TYPE在UI上会展示收发两种不同的布局，定义一组customType来对应
public var SEND_GIFT_TYPE_SEND: Int = 11006

public var SEND_GIFT_TYPE_RECV: Int = 11007
