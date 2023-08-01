// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
public protocol NEOneOnOneListener: NSObjectProtocol {
  /// 接收消息回调
  /// - Parameter message: 消息对象
  @objc optional func onReceiveCustomMessage(message: NEOneOnOneCustomMessage)
  /// 接收通知消息回调
  /// - Parameter message: 消息对象
  @objc optional func onReceiveNotificationCustomMessage(message: NEOneOnOneCustomMessage)

  /// 接收到邀请回调
//  @objc optional func onOneOnOneInvited(_ invitor: String, userIDs: [String], isFromGroup: Bool, groupID: String?, type: NEOneOnOneRtcCallType, attachment: String?)

  /// 接收到礼物
  @objc optional func onReceiveGift(gift: NEOneOnOneOneGift)
}
