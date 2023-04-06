// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERtcCallKit
import NIMSDK

extension NEOneOnOneKit: NIMLoginManagerDelegate {
  /// 添加登录监听
  /// - Parameter listener: 监听器
  public func addAuthListener(_ listener: NEOneOnOneAuthListener) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Add auth listener.")
    authListeners.addWeakObject(listener)
  }

  /// 移除登录监听
  /// - Parameter listener: 监听器
  public func removeAuthListener(_ listener: NEOneOnOneAuthListener) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Remove auth listener.")
    authListeners.removeWeakObject(listener)
  }

  /// 登录
  /// - Parameters:
  ///   - account: 账号
  ///   - token: 令牌
  ///   - callback: 回调
  public func login(_ account: String,
                    nickName: String?,
                    avatar: String?,
                    token: String,
                    imToken: String,
                    resumeLogin: Bool = false,
                    callback: NEOneOnOneCallback<AnyObject>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Login. Account: \(account). Token: \(token) .imToken:\(imToken)")

    guard NEOneOnOneKit.getInstance().isInitialized else {
      NEOneOnOneLog.errorLog(kitTag, desc: "Failed to login. Uninitialized.")
      callback?(NEOneOnOneErrorCode.failed, "Failed to login. Uninitialized.", nil)
      return
    }

    /// 设置RTC Uid 和account一致
    let accountIntValue = Int(account) ?? 0
    if accountIntValue == 0 {
      callback?(NEOneOnOneErrorCode.failed, "error Account", nil)
      return
    }
    NERtcCallKit.sharedInstance().setValue(NSNumber(value: accountIntValue), forKeyPath: "context.currentUserUid")

    if resumeLogin {
      _localMember.mobile = NIMSDK.shared().userManager.userInfo(account)?.userInfo?.mobile ?? ""
      _localMember.imToken = imToken
      _localMember.imAccid = account
      _localMember.avatar = avatar ?? ""
      _localMember.nickName = nickName ?? ""
      // 1v1 需要使用以下代码
      NE.addHeader([
        "accountId": account,
        "accessToken": token,
        "appKey": config?.appKey ?? "",
      ])

      /// 启动定时器
      roomService.startRepoty()
      callback?(NEOneOnOneErrorCode.success, nil, nil)
      return
    }

    // IM 登录
    NERtcCallKit.sharedInstance().login(account, token: imToken) { error in
      if let error = error as NSError? {
        NEOneOnOneLog.errorLog(kitTag, desc: "Failed to login. Code: \(error.code)")
        callback?(error.code, error.debugDescription, nil)
        return
      }

      NEOneOnOneLog.successLog(kitTag, desc: "Successfully login.")
      // 登陆成功后，headers添加属性

      self._localMember.mobile = NIMSDK.shared().userManager.userInfo(account)?.userInfo?.mobile ?? ""
      self._localMember.imToken = imToken
      self._localMember.imAccid = account
      self._localMember.avatar = avatar ?? ""
      self._localMember.nickName = nickName ?? ""
      // 1v1 需要使用以下代码
      NE.addHeader([
        "accountId": account,
        "accessToken": token,
        "appKey": self.config?.appKey ?? "",
      ])

      /// 启动定时器
      self.roomService.startRepoty()
      callback?(NEOneOnOneErrorCode.success, nil, nil)
    }
  }

  /// 退出登录
  /// - Parameter callback: 回调
  public func logout(callback: NEOneOnOneCallback<AnyObject>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Logout.")

    guard NEOneOnOneKit.getInstance().isInitialized else {
      NEOneOnOneLog.errorLog(kitTag, desc: "Failed to logout. Uninitialized.")
      callback?(NEOneOnOneErrorCode.failed, "Failed to logout. Uninitialized.", nil)
      return
    }

    NERtcCallKit.sharedInstance().logout { error in
      if let error = error as? NSError {
        NEOneOnOneLog.errorLog(kitTag, desc: "Failed to logout. Code: \(error.code)")
        callback?(error.code, error.description, nil)
      } else {
        // 取消上报
        self.roomService.cancelReport()
        callback?(NEOneOnOneErrorCode.success, nil, nil)
        NEOneOnOneLog.successLog(kitTag, desc: "Successfully logout.")
      }
    }
  }

  // MARK: NIM auth 回调

  public func onKickout(_ result: NIMLoginKickoutResult) {
    roomService.cancelReport()
    NEOneOnOneLog.infoLog(
      "NERoomNotificationCenter",
      desc: "⚠️IMSDK kickout. Result: \(result.reasonDesc)"
    )
    for pointerListener in listeners.allObjects {
      if pointerListener is NEOneOnOneAuthListener {
        let listener = pointerListener as! NEOneOnOneAuthListener
        if listener.responds(to: #selector(NEOneOnOneAuthListener.onOneOnOneAuthEvent(_:))) {
          listener.onOneOnOneAuthEvent?(.loggedOut)
        }
      }
    }
  }

  public func onLogin(_ step: NIMLoginStep) {
    NEOneOnOneLog.infoLog(
      "NERoomNotificationCenter",
      desc: "⚠️IMSDK login step. Step: \(step.rawValue)"
    )
    if step == .loginOK {
      for pointerListener in listeners.allObjects {
        if pointerListener is NEOneOnOneAuthListener {
          let listener = pointerListener as! NEOneOnOneAuthListener
          if listener
            .responds(to: #selector(NEOneOnOneAuthListener.onOneOnOneAuthEvent(_:))) {
            listener.onOneOnOneAuthEvent?(.loggedIn)
          }
        }
      }
    } else if step == .loginFailed {
      for pointerListener in listeners.allObjects {
        if pointerListener is NEOneOnOneAuthListener {
          let listener = pointerListener as! NEOneOnOneAuthListener
          if listener
            .responds(to: #selector(NEOneOnOneAuthListener.onOneOnOneAuthEvent(_:))) {
            listener.onOneOnOneAuthEvent?(.forbidden)
          }
        }
      }
    }
  }

  public func updateNickName(_ nickName: String) {
    _localMember.nickName = nickName
  }
}
