// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERtcCallKit

public extension NEOneOnOneKit {
  /// 查询房间列表
  /// - Parameters:
  ///   - liveState: 直播状态
  ///   - pageNum: 页码
  ///   - pageSize: 页大小
  ///   - callback: 房间列表回调
  func getOneOnOneList(_ pageNum: Int,
                       pageSize: Int,
                       callback: NEOneOnOneCallback<NEOneOnOneOnlineUserList>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "Room List.")
    Judge.initCondition({
      self.roomService.getOneOnOneList(
        pageNum: pageNum,
        pageSize: pageSize
      ) { list in
        NEOneOnOneLog.successLog(kitTag, desc: "Successfully get room list.")
        callback?(NEOneOnOneErrorCode.success, nil, list)
      } failure: { error in
        NEOneOnOneLog.errorLog(
          kitTag,
          desc: "Failed to get room list. Code: \(error.code). Msg: \(error.localizedDescription)"
        )
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  func getAccountInfo(_ userUuid: String, callback: NEOneOnOneCallback<NEOneOnOneAccountInfo>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "getAccountInfo")
    Judge.initCondition({
      self.roomService.getAccountInfo(userUuid) { info in
        NEOneOnOneLog.successLog(kitTag, desc: "Successfully get account info.")
        callback?(NEOneOnOneErrorCode.success, nil, info)
      } failure: { error in
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }

  func loginGetRTCUid(_ callback: NEOneOnOneCallback<NEOneOnOneAccountInfo>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "getAccountInfo")
    Judge.initCondition({
      self.roomService.loginGetRTCUid({ info in
        NEOneOnOneLog.successLog(kitTag, desc: "Successfully get account info.")
        callback?(NEOneOnOneErrorCode.success, nil, info)

      }, failure: { error in
        callback?(error.code, error.localizedDescription, nil)
      })
    }, failure: callback)
  }

  func getUserState(_ mobile: String, callback: NEOneOnOneCallback<String>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "getUserStateIsOneline")
    Judge.initCondition({
      self.roomService.getUserState(mobile) { onlineState in
        callback?(NEOneOnOneErrorCode.success, nil, onlineState)
      } failure: { error in
        callback?(error.code, error.localizedDescription, nil)
      }

    }, failure: callback)
  }

  func reward(giftId: Int, giftCount: Int, target: String, callback: NEOneOnOneCallback<AnyObject>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "reward")
    Judge.initCondition({
      self.roomService.reward(giftId: giftId, giftCount: giftCount, target: target) {
        callback?(NEOneOnOneErrorCode.success, nil, nil)
      } failure: { error in
        callback?(error.code, error.localizedDescription, nil)
      }

    }, failure: callback)
  }

  /// 是否在1V1房间中
  func isInOneOnOne() -> Bool {
    if let callId = NECallEngine.sharedInstance().getCallInfo()?.callId,
       callId.count > 0 {
      return true
    }
    return false
  }

  // 创建房间后上报
  func reportRtcRoom(_ cid: Int64, callback: NEOneOnOneCallback<AnyObject>? = nil) {
    NEOneOnOneLog.apiLog(kitTag, desc: "reportRtcRoom,cid:\(cid)")
    Judge.initCondition({
      self.roomService.reportRtcRoom(cid: cid) {
        callback?(NEOneOnOneErrorCode.success, nil, nil)
      } failure: { error in
        callback?(error.code, error.localizedDescription, nil)
      }
    }, failure: callback)
  }
}
