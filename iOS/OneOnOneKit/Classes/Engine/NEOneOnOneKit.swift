// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERtcCallKit
import NIMSDK
import NECoreIMKit

let kitTag = "NEOneOnOneKit"
@objcMembers
public class NEOneOnOneKit: NSObject {
  // MARK: - ------------------------- Public method --------------------------

  /// 单例初始化
  /// - Returns: 单例对象
  public static func getInstance() -> NEOneOnOneKit {
    instance
  }

  /// 本端成员信息
  public var localMember: NEOneOnOneUser? {
    _localMember
  }

  /// 拦截器
  public var interceptor: NEOneOnOneInterceptor?

  /// NEOneOnOneKit 初始化
  /// - Parameters:
  ///   - config: 初始化配置
  ///   - callback: 回调
  public func initialize(_ config: NEOneOnOneKitConfig,
                         callback: NEOneOnOneCallback<AnyObject>? = nil) {
    NEOneOnOneLog.setUp(config.appKey)
    NEOneOnOneLog.apiLog(kitTag, desc: "Initialize")
    self.config = config

    /// 非私有化 且要出海 使用默认海外环境
    var overseaAndNotPrivte = false
    if let serverUrl = config.extras["serverUrl"] {
      isDebug = serverUrl == "test"
      isOversea = serverUrl == "oversea"
      if !serverUrl.contains("http"), isOversea {
        overseaAndNotPrivte = true
        config.extras["serverUrl"] = "https://roomkit-sg.netease.im"
      }
    }
    NE.config.isDebug = isDebug
    NE.config.isOverSea = isOversea
    let option = NERtcCallOptions()
    option.apnsCerName = config.APNSCerName
    option.voIPCerName = config.VoIPCerName ?? ""
    option.globalInit = false
//      option.joinRtcWhenCall = true

    if overseaAndNotPrivte {
      NIMSDK.shared().serverSetting.lbsAddress = "https://lbs.netease.im/lbs/conf.jsp"
      NIMSDK.shared().serverSetting.linkAddress = "link-sg.netease.im:7000"
    }
//    NERtcCallKit.sharedInstance().setupAppKey(config.appKey, withRtcUid: UInt64(account.longLongValue), options: option)
    /// CallKit 初始化
    let context = NERtcEngineContext()
    context.appKey = config.appKey
    let logSetting = NERtcLogSetting()
    logSetting.logLevel = .info
    logSetting.logDir = rtcLogPath()
    context.logSetting = logSetting
    NERtcCallKit.sharedInstance().setupAppKey(config.appKey, options: option, with: context)
    /// CoreImKit 初始化
    let NIMOption = NIMSDKOption()
    NIMOption.appKey = config.appKey
    NIMOption.apnsCername = config.APNSCerName
    IMKitClient.instance.setupCoreKitIM(NIMOption)

    isInitialized = true
    NEOneOnOneLog.successLog(kitTag, desc: "Successfully initialize.")

    callback?(NEOneOnOneErrorCode.success, nil, nil)
  }

  func rtcLogPath() -> String {
    NSHomeDirectory() + "/Documents/NIMSDK/Logs/extra_log"
  }

  /// 初始化状态
  public var isInitialized: Bool = false

  /// 添加房间监听
  /// - Parameter listener: 事件监听

  public func addOneOnOneListener(_ listener: NEOneOnOneListener) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Add OneOnOne listener.")
    listeners.addWeakObject(listener)
  }

  /// 移除房间监听
  /// - Parameter listener: 事件监听
  public func removeOneOnOneListener(_ listener: NEOneOnOneListener) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Remove OneOnOne listener.")
    listeners.removeWeakObject(listener)
  }

  /// 取消在线上报
  public func cancelReport() {
    roomService.cancelReport()
  }

  // MARK: - ------------------------- Private method --------------------------

  override init() {
    super.init()
    /// 添加监听
    NERtcCallKit.sharedInstance().add(self)
    NIMSDK.shared().loginManager.add(self)
    NIMSDK.shared().passThroughManager.add(self)
    NIMSDK.shared().chatManager.add(self)
    NIMSDK.shared().systemNotificationManager.add(self)
  }

  deinit {
    /// 移除监听
    NERtcCallKit.sharedInstance().remove(self)
    NIMSDK.shared().loginManager.remove(self)
    NIMSDK.shared().passThroughManager.remove(self)
    NIMSDK.shared().systemNotificationManager.remove(self)
  }

  private static let instance = NEOneOnOneKit()
  // 房间监听器数组
  var listeners = NSPointerArray.weakObjects()

  // 本端用户数据
  var _localMember = NEOneOnOneUser()
  // 登录监听器数组
  var authListeners = NSPointerArray.weakObjects()

  var config: NEOneOnOneKitConfig?
  var isDebug: Bool = false
  /// 是否出海
  var isOversea: Bool = false

  /// 自动上报
  var timer: Timer?
  // 维护房间上下文
//  var roomContext: NERoomContext?
  // 维护预览房间上下文
//  var previewRoomContext: NEPreviewRoomContext?

  // 房间服务
  private var _roomService = NEOneOnOneRoomService()
  internal var roomService: NEOneOnOneRoomService { _roomService }
}
