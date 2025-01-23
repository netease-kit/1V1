// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 登录事件枚举
@objc
public enum NEOneOnOneAuthEvent: UInt {
  /// 被踢出登录
  case kickOut
  /// 服务器禁止登录
  case forbidden
  /// 账号或密码错误
  case accountTokenError
  /// 登录成功
  case loggedIn
  /// 未登录
  case loggedOut
  /// 授权错误
  case incorrectToken
  /// Token过期
  case tokenExpired
}

/// 鉴权监听协议
@objc
public protocol NEOneOnOneAuthListener: NSObjectProtocol {
  /// 登录状态变更
  /// - Parameter event: 鉴权事件枚举
  @objc optional func onOneOnOneAuthEvent(_ event: NEOneOnOneAuthEvent)
}
