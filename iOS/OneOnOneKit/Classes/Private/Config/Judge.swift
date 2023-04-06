// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct Judge {
  /// 前置条件判断
//  static func preCondition<T: Any>(_ success: @escaping () -> Void,
//                                   failure: NEOneOnOneCallback<T>? = nil) {
//    guard NEOneOnOneKit.getInstance().isInitialized else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
//      failure?(NEOneOnOneErrorCode.failed, "Uninitialized.", nil)
//      return
//    }
//    guard let _ = NEOneOnOneKit.getInstance().roomContext else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "RoomContext not exist.")
//      failure?(NEOneOnOneErrorCode.failed, "RoomContext not exist.", nil)
//      return
//    }
//    success()
//  }

//  /// 前置条件判断
//  static func prePreViewCondition<T: Any>(_ success: @escaping () -> Void,
//                                          failure: NEOneOnOneCallback<T>? = nil) {
//    guard NEOneOnOneKit.getInstance().isInitialized else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
//      failure?(NEOneOnOneErrorCode.failed, "Uninitialized.", nil)
//      return
//    }
//    guard let _ = NEOneOnOneKit.getInstance().previewRoomContext else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "previewRoomContext not exist.")
//      failure?(NEOneOnOneErrorCode.failed, "previewRoomContext not exist.", nil)
//      return
//    }
//    success()
//  }

  /// 初始化判断条件
  static func initCondition<T: Any>(_ success: @escaping () -> Void,
                                    failure: NEOneOnOneCallback<T>? = nil) {
    guard NEOneOnOneKit.getInstance().isInitialized else {
      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
      failure?(NEOneOnOneErrorCode.failed, "Uninitialized.", nil)
      return
    }
    success()
  }

//  @discardableResult

//  /// 同步返回
//  static func syncCondition(_ success: @escaping () -> Int) -> Int {
//    guard NEOneOnOneKit.getInstance().isInitialized else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
//      return NEOneOnOneErrorCode.failed
//    }
//    guard let _ = NEOneOnOneKit.getInstance().roomContext else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "RoomContext is nil.")
//      return NEOneOnOneErrorCode.failed
//    }
//    return success()
//  }

//  /// 同步返回
//  static func syncPreviewCondition(_ success: @escaping () -> Int) -> Int {
//    guard NEOneOnOneKit.getInstance().isInitialized else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
//      return NEOneOnOneErrorCode.failed
//    }
//    guard let _ = NEOneOnOneKit.getInstance().previewRoomContext else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "previewRoomContext is nil.")
//      return NEOneOnOneErrorCode.failed
//    }
//    return success()
//  }

//  static func condition(_ success: @escaping () -> Void) {
//    guard NEOneOnOneKit.getInstance().isInitialized else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
//      return
//    }
//    guard let _ = NEOneOnOneKit.getInstance().roomContext else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "RoomContext is nil.")
//      return
//    }
//    success()
//  }

//  static func syncResult<T: Any>(_ success: @escaping () -> T) -> T? {
//    guard NEOneOnOneKit.getInstance().isInitialized else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "Uninitialized.")
//      return nil
//    }
//    guard let _ = NEOneOnOneKit.getInstance().roomContext else {
//      NEOneOnOneLog.errorLog(kitTag, desc: "RoomContext is nil.")
//      return nil
//    }
//    return success()
//  }
}
