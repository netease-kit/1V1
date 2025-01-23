// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECommonUIKit
import NERtcCallKit
import NERtcCallUIKit

@objcMembers public class NEOneOnOneUIKitCallEngine: NSObject, NECallUIKitDelegate {
  public var baseOnWindow: UIWindow?
  /// 单例初始化
  public static let getInstance: NEOneOnOneUIKitCallEngine = .init()
  /// 是否在进行呼叫中
  private var isEnterRoom = false

  /// 注意！！！单对象属性！！！已经被使用！！！不要替换了！！！
  /// 呼叫页面即将被唤起
  public var callComing: (() -> Void)?

  override public init() {
    super.init()
    /// 添加通知
    NotificationCenter.default.addObserver(self, selector: #selector(busyViewShouHide), name: NSNotification.Name(NEOneOnOneCallViewControllerAppear), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(showBusyView), name: NSNotification.Name(NEOneOnOneCallViewControllerBusy), object: nil)

    let config = NECallUIKitConfig()
//      config.uiConfig.showCallingSwitchCallType = true;
//      config.uiConfig.enableVideoToAudio = true;
//      config.uiConfig.enableAudioToVideo = true;
    // 开启应用内小窗
    config.uiConfig.enableFloatingWindow = true
    // 开启应用外小窗，目前只支持iOS16+
    config.uiConfig.enableFloatingWindowOutOfApp = true

    NERtcCallUIKit.sharedInstance().setup(with: config)

    NERtcCallUIKit.sharedInstance().customControllerClass = NEOneOnOneCallViewController.self
    NERtcCallUIKit.sharedInstance().delegate = self
  }

  /// 对方是否忙碌
  func busyViewShouHide() {
    busyView.removeFromSuperview()
  }

  // 展示忙碌视图
  func showBusyView() {
    DispatchQueue.main.async {
//        if ([[UIApplication sharedApplication].delegate respondsToSelector:@selector(window)]) {
//            [[UIApplication sharedApplication].delegate window]
//        }
//      print("window--- :\(String(describing: self.baseOnWindow))")
      self.baseOnWindow?.addSubview(self.busyView)
    }
  }

  // 忙碌视图
  lazy var busyView: NEOneOnOneUserBusyView = {
    let busyView = NEOneOnOneUserBusyView(frame: UIScreen.main.bounds)
    return busyView
  }()

  // MARK: Engine Call

  // 具体处理

  public func dealWithAction(_ controller: UIViewController, sessionId: String, isAudio: Bool, isVirtualRoom: Bool, nickName: String?, icon: String?, audioUrl: String?, videoUrl: String?) {
    if isEnterRoom {
      return
    }

    // 已点击通话数据

    isEnterRoom = true

    // 判断自身是否忙碌

    // 用户不在RTC房间中
    if let canContinueAction = NEOneOnOneUIManager.sharedInstance().canContinueAction,
       !canContinueAction() {
      /// 不能拨号
      /// 弹出提示框
      // 取消已点击通话数据
      isEnterRoom = false

    } else {
      // 外部未实现,则认为不需要处理，用户处于闲置中
      startCallKitJudgeUserBusy(controller, sessionId: sessionId, isAudio: isAudio, isVirtualRoom: isVirtualRoom, nickName: nickName, icon: icon, audioUrl: audioUrl, videoUrl: videoUrl)
    }
  }

  func startCallKitJudgeUserBusy(_ controller: UIViewController, sessionId: String, isAudio: Bool, isVirtualRoom: Bool, nickName: String?, icon: String?, audioUrl: String?, videoUrl: String?) {
    NEOneOnOneKit.getInstance().getAccountInfo(sessionId) { code, msg, accountInfo in
      if code != 0 {
        // 请求失败
        NEOneOnOneToast.show(ne_oneOnOne_localized("网络异常，请稍后重试"))
        // 取消已点击通话数据
        self.isEnterRoom = false
      } else {
        guard let mobile = accountInfo?.mobile else {
          // error mobileCode
          NEOneOnOneToast.show(ne_oneOnOne_localized("账号异常"))
          self.isEnterRoom = false
          return
        }

        NEOneOnOneKit.getInstance().getUserState(mobile, callback: { [weak self] code, msg, onlineState in
          if code == 0 {
            // 请求成功
            if onlineState != nil, onlineState == "online" {
              // 在线
              /// 直接呼叫，对端收到邀请通知，如果在忙的话，hungUp配上原因，本段可以拿到状态
              self?.startCallAction(controller, sessionId: sessionId, isAudio: isAudio, isVirtualRoom: isVirtualRoom, nickName: nickName, icon: icon, audioUrl: audioUrl, videoUrl: videoUrl)
            } else {
              // 不在线
              NEAlertViewController.presentAlertViewController("", messageContent: ne_oneOnOne_localized("对方不在线，请稍后再试"), cancelTitle: nil, confirmTitle: ne_oneOnOne_localized("确定"))
              // 取消已点击通话数据
              self?.isEnterRoom = false
              return
            }
          } else {
            // 请求失败
            NEOneOnOneToast.show(ne_oneOnOne_localized("网络异常，请稍后重试"))
            // 取消已点击通话数据
            self?.isEnterRoom = false
          }
        })
      }
    }
  }

  func startCallAction(_ controller: UIViewController, sessionId: String, isAudio: Bool, isVirtualRoom: Bool, nickName: String?, icon: String?, audioUrl: String?, videoUrl: String?) {
    var hasPermissions = false

    let semaphore = DispatchSemaphore(value: 0)

    if isAudio {
      NEOneOnOneUIKitUtils.getMicrophonePermissions(AVMediaType.audio) { authorized in
        if authorized {
          hasPermissions = true
          semaphore.signal()
        } else {
          NEOneOnOneToast.show(ne_oneOnOne_localized("麦克风权限已关闭，请开启后重试"))
          semaphore.signal()
        }
      }
    } else {
      NEOneOnOneUIKitUtils.getMicrophonePermissions(AVMediaType.audio) { authorized in
        if authorized {
          NEOneOnOneUIKitUtils.getMicrophonePermissions(AVMediaType.video) { authorized in
            if authorized {
              hasPermissions = true
              semaphore.signal()
            } else {
              NEOneOnOneToast.show(ne_oneOnOne_localized("摄像头权限已关闭，请开启后重试"))
              semaphore.signal()
            }
          }
        } else {
          NEOneOnOneUIKitUtils.getMicrophonePermissions(AVMediaType.video) { authorized in
            if authorized {
              NEOneOnOneToast.show(ne_oneOnOne_localized("麦克风权限已关闭，请开启后重试"))
              semaphore.signal()
            } else {
              NEOneOnOneToast.show(ne_oneOnOne_localized("麦克风和摄像头权限已关闭，请开启后重试"))
              semaphore.signal()
            }
          }
        }
      }
    }
    semaphore.wait()
    if !hasPermissions {
      isEnterRoom = false
      return
    }
    print("权限判断通过")

    if !isVirtualRoom {
      NECallEngine.sharedInstance().timeOutSeconds = isAudio ? 15 : 30
    }

    let attachment: [String: Any] = [
      CALLER_USER_NAME: NEOneOnOneKit.getInstance().localMember?.nickName as Any,
      CALLER_USER_MOBILE: NEOneOnOneKit.getInstance().localMember?.mobile as Any,
      CALLER_USER_AVATAR: NEOneOnOneKit.getInstance().localMember?.avatar as Any,
    ]

    let jsonData = try? JSONSerialization.data(withJSONObject: attachment, options: .prettyPrinted)

    let jsonString = String(data: jsonData ?? Data(), encoding: .utf8)

    DispatchQueue.main.async { [weak self] in
      guard let strongSelf = self else { return }
      if isVirtualRoom {
        let callViewController = NEOneOnOneCallViewController()
        NECallEngine.sharedInstance().changeStatusCalling()
        strongSelf.startCallKit(controller, sessionId: sessionId, isAudio: isAudio, isVirtualRoom: isVirtualRoom, nickName: nickName, icon: icon, audioUrl: audioUrl, videoUrl: videoUrl, callViewController: callViewController)
        return
      }

      let param = NECallParam(accId: sessionId, with: isAudio ? .audio : .video)
      param.extraInfo = jsonString

      NECallEngine.sharedInstance().call(param) { [weak self] error, info in
        print("\(String(describing: error))")
        if let error = error as NSError?, error.code != 0 {
          NEOneOnOneToast.show(ne_oneOnOne_localized("呼叫未成功发出"))
          self?.isEnterRoom = false
        } else {
          self?.startCallKit(controller, sessionId: sessionId, isAudio: isAudio, isVirtualRoom: isVirtualRoom, nickName: nickName, icon: icon, audioUrl: audioUrl, videoUrl: videoUrl, callViewController: nil)
        }
      }
    }
  }

  func startCallKit(_ controller: UIViewController, sessionId: String, isAudio: Bool, isVirtualRoom: Bool, nickName: String?, icon: String?, audioUrl: String?, videoUrl: String?, callViewController: NEOneOnOneCallViewController?) {
    if isVirtualRoom, callViewController != nil {
      let remoteUser = NEOneOnOneOnlineUser()

      remoteUser.userUuid = sessionId

      remoteUser.userName = nickName

      remoteUser.icon = icon
      remoteUser.callType = isVirtualRoom ? 1 : 0
      remoteUser.audioUrl = audioUrl
      remoteUser.videoUrl = videoUrl

      callViewController!.remoteUser = remoteUser
      let callParam: NEUICallParam = .init()
      callParam.remoteUserAccid = sessionId
      callParam.remoteShowName = nickName ?? ""
      callParam.remoteAvatar = icon ?? ""
      callViewController!.callParam = callParam
      if isAudio {
        callViewController!.enterStatus = NEEnterStatus(rawValue: 0)
      } else {
        callViewController!.enterStatus = NEEnterStatus(rawValue: 1)
      }
      callViewController!.modalPresentationStyle = .overFullScreen
      if let presentedViewController = controller.presentedViewController {
        presentedViewController.present(callViewController!, animated: true)
      } else {
        controller.present(callViewController!, animated: true)
      }

      DispatchQueue.main.asyncAfter(deadline: .now() + 1) { [weak self] in
        self?.isEnterRoom = false
      }
      return
    }

    let callParam = NEUICallParam()
    callParam.remoteUserAccid = sessionId
    callParam.remoteShowName = nickName ?? ""
    callParam.remoteAvatar = icon ?? ""

    if isAudio {
      callParam.callType = .audio
    } else {
      callParam.callType = .video
    }
    let callPushConfig = NECallPushConfig()
    let pushContent = isAudio ? "\(NEOneOnOneKit.getInstance().localMember?.nickName ?? "") \(ne_oneOnOne_localized("邀请您语音聊天"))" : "\(NEOneOnOneKit.getInstance().localMember?.nickName ?? "") \(ne_oneOnOne_localized("邀请您视频聊天"))"
    callPushConfig.pushContent = pushContent
    callParam.pushConfig = callPushConfig
    NERtcCallUIKit.sharedInstance().call(with: callParam)

    DispatchQueue.main.asyncAfter(deadline: .now() + 1) { [weak self] in
      self?.isEnterRoom = false
    }
  }

  // MARK: NECallUIKitDelegate

  public func didCallComing(with inviteInfo: NEInviteInfo, with callParam: NEUICallParam, withCompletion completion: @escaping (Bool) -> Void) {
    let needInterceptor: Bool = NEOneOnOneUIManager.sharedInstance().interceptor()
    // 是否需要拦截
    if needInterceptor {
      let param = NEHangupParam()
      param.setValue(NSInteger(NERtcCallTerminalCode.TerminalCodeBusy.rawValue), forKey: "reasonCode")
      NECallEngine.sharedInstance().hangup(param)
      return
    }

    // 如果是黑名单成员，直接拒接
    if NIMSDK.shared().userManager.isUser(inBlackList: inviteInfo.callerAccId) {
      NECallEngine.sharedInstance().hangup(NEHangupParam())
      return
    }
    callComing?()
    completion(true)
  }

  // 外部调用挂断的时候需要调用此方法，因为还需要发送通知，将小窗消失掉，后期呼叫组件优化后，调用内部hangup的效果相同
  public func hangUpOutside() {
    NECallEngine.sharedInstance().hangup(NEHangupParam(), completion: nil)
    // 这行代码应该删除，现在退出登录后，呼叫组件没有主动消失小窗，所以需要特殊处理
    NotificationCenter.default.post(name: NSNotification.Name(kCallKitDismissNoti), object: nil)
  }
}
