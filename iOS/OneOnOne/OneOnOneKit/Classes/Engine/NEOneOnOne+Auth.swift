// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECoreIM2Kit
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
    /// 注释以下代码：RTC通过接口获取，不进行account是否为数字的判断
//    let accountIntValue = Int(account) ?? 0
//    if accountIntValue == 0 {
//      callback?(NEOneOnOneErrorCode.failed, "error Account", nil)
//      return
//    }

    if resumeLogin {
      _localMember.mobile = NIMSDK.shared().userManager.userInfo(account)?.userInfo?.mobile ?? ""
      _localMember.imToken = imToken
      _localMember.imAccid = account
      _localMember.avatar = avatar ?? ""
      _localMember.nickName = nickName ?? ""
      // 1v1 需要使用以下代码
      NE.addHeader([
        "user": account,
        "token": token,
        "appKey": config?.appKey ?? "",
      ])

      roomService.loginGetRTCUid({ accountInfo in
        if let rtcUid = accountInfo?.rtcUid {
          NECallEngine.sharedInstance().setValue(NSNumber(value: rtcUid), forKeyPath: "context.currentUserUid")
          self.localMember?.rtcUid = rtcUid
          /// 启动定时器
          self.roomService.startRepoty()
          callback?(NEOneOnOneErrorCode.success, nil, nil)
        } else {
          NEOneOnOneLog.errorLog(kitTag, desc: "Failed to loginGetRTCUid.")
          callback?(NEOneOnOneErrorCode.failed, "Failed to loginGetRTCUid.", nil)
        }
      }) { error in
        if let error = error as NSError? {
          NEOneOnOneLog.errorLog(kitTag, desc: "Failed to loginGetRTCUid. Code: \(error.code)")
          callback?(error.code, error.debugDescription, nil)
        }
      }
      return
    }
    // headers添加属性
    // 1v1 需要使用以下代码
    NE.addHeader([
      "user": account,
      "token": token,
      "appKey": config?.appKey ?? "",
    ])

    roomService.loginGetRTCUid({ accountInfo in
      if let rtcUid = accountInfo?.rtcUid {
        NECallEngine.sharedInstance().setValue(NSNumber(value: rtcUid), forKeyPath: "context.currentUserUid")
        self.localMember?.rtcUid = rtcUid
        self.localMember?.imToken = imToken
        self.localMember?.imAccid = account
        self.localMember?.avatar = avatar ?? ""
        self.localMember?.nickName = nickName ?? ""
        self.localMember?.mobile = accountInfo?.mobile ?? ""
        // IM 登录
        let option = V2NIMLoginOption()
        IMKitClient.instance.login(account, token, option) { error in
          if let error = error as NSError? {
            NEOneOnOneLog.errorLog(kitTag, desc: "Failed to login. Code: \(error.code)")
            callback?(error.code, error.debugDescription, nil)
            return
          } else {
            NEOneOnOneLog.successLog(kitTag, desc: "Successfully login IM.")
            /// 启动定时器
            self.roomService.startRepoty()
            callback?(NEOneOnOneErrorCode.success, nil, nil)
          }
        }

      } else {
        NEOneOnOneLog.errorLog(kitTag, desc: "Failed to loginGetRTCUid.")
        callback?(NEOneOnOneErrorCode.failed, "Failed to loginGetRTCUid.", nil)
      }
    }) { error in
      if let error = error as NSError? {
        NEOneOnOneLog.errorLog(kitTag, desc: "Failed to loginGetRTCUid. Code: \(error.code)")
        callback?(error.code, error.debugDescription, nil)
      }
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

    IMKitClient.instance.logoutIM { error in
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
          listener.onOneOnOneAuthEvent?(.kickOut)
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
