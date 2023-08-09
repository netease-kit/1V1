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

  /// 根据userUuid获取账号信息
  func getAccountInfo(_ userUuid: String, success: ((NEOneOnOneAccountInfo?) -> Void)? = nil,
                      failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "userUuid": userUuid,
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

  /// 登录获取RTCUID
  func loginGetRTCUid(_ success: ((NEOneOnOneAccountInfo?) -> Void)? = nil,
                      failure: ((NSError) -> Void)? = nil) {
    NEAPI.OneOnOne.loginGetRTCUid.request(returnType: _NEOneOnOneAccountInfo.self) { data in
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

  /// 心跳上报
  func userReport(success: (() -> Void)? = nil,
                  failure: ((NSError) -> Void)? = nil) {
    NEAPI.OneOnOne.userReport.request { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  func reward(giftId: Int, giftCount: Int, target: String, success: (() -> Void)? = nil,
              failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "giftId": giftId,
      "giftCount": giftCount,
      "target": target,
    ]
    NEAPI.OneOnOne.reward.request(params) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }

  func startRepoty() {
    queue.async {
      self.cancelReport()
      self.reportTimer = Timer(timeInterval: 4, target: self, selector: #selector(self.reportTimerAction), userInfo: nil, repeats: true)

      if self.reportTimer != nil {
        RunLoop.current.add(self.reportTimer!, forMode: .common)
        CFRunLoopRun()
        self.reportTimer?.fire()
      }
    }
  }

  @objc func reportTimerAction() {
    userReport {} failure: { error in
      print("error:\(error.description)")
    }
  }

  func cancelReport() {
    if let timer = reportTimer {
      timer.invalidate()
      reportTimer = nil
    }
  }

  func reportRtcRoom(cid: Int64, success: (() -> Void)? = nil,
                     failure: ((NSError) -> Void)? = nil) {
    let params: [String: Any] = [
      "cid": cid,
    ]
    NEAPI.OneOnOne.reportRtc.request(params) { _ in
      success?()
    } failed: { error in
      failure?(error)
    }
  }
}
