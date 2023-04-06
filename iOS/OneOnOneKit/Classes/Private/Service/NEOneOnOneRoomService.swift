// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

class NEOneOnOneRoomService {
  internal var reportTimer: Timer?

  let queue = DispatchQueue(label: "timerQueue", attributes: .concurrent)

  /// 获取房间列表
  /// - Parameters:
  ///   - type: 房间类型，默认为2：ChatRoom
  ///   - liveState: 房间状态
  ///   - pageNum: 每页数量
  ///   - pageSize: 页号
  ///   - callback: 回调
  func getOneOnOneList(pageNum: Int,
                       pageSize: Int,
                       success: ((NEOneOnOneOnlineUserList?) -> Void)? = nil,
                       failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "pageNum": pageNum,
      "pageSize": pageSize,
    ]
    NEAPI.OneOnOne.roomList.request(params,
                                    returnType: _NEOneOnOneOnlineUserList.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      let roomList = NEOneOnOneOnlineUserList(data)
      success?(roomList)
    } failed: { error in
      failure?(error)
    }
  }

  /// 根据手机号获取用户信息
  func getAccountInfo(_ mobile: String, success: ((NEOneOnOneAccountInfo?) -> Void)? = nil,
                      failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "mobile": mobile,
    ]
    NEAPI.OneOnOne.accountInfo.request(params, returnType: _NEOneOnOneAccountInfo.self) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      let accountInfo = NEOneOnOneAccountInfo(data)
      success?(accountInfo)
    } failed: { error in
      failure?(error)
    }
  }

  func getUserState(_ mobile: String, success: ((String?) -> Void)? = nil,
                    failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "mobile": mobile,
    ]
    NEAPI.OneOnOne.userState.request(params) { data in
      guard let data = data else {
        success?(nil)
        return
      }
      let state = data
      success?(state["data"] as? String)
    } failed: { error in
      failure?(error)
    }
  }

  /// 心跳上报
  func userReport(success: (() -> Void)? = nil,
                  failure: ((NSError) -> Void)? = nil) {
    NEAPI.OneOnOne.userReport.request { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  func startRepoty() {
    queue.async {
      self.cancelReport()
      self.reportTimer = Timer(timeInterval: 5, target: self, selector: #selector(self.reportTimerAction), userInfo: nil, repeats: true)

      if self.reportTimer != nil {
        RunLoop.current.add(self.reportTimer!, forMode: .common)
        CFRunLoopRun()
        self.reportTimer?.fire()
      }
    }
  }

  @objc func reportTimerAction() {
    userReport {
      print("上报完成")
    } failure: { error in
      print("error:\(error.description)")
    }
  }

  func cancelReport() {
    if let timer = reportTimer {
      timer.invalidate()
      reportTimer = nil
    }
  }
}
