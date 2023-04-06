// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NERtcCallKit

extension NEOneOnOneKit: NERtcCallKitDelegate {
  public func onInvited(_ invitor: String, userIDs: [String], isFromGroup: Bool, groupID: String?, type: NERtcCallType, attachment: String?) {
    /// 获取本段判断
    if let interceptor = interceptor {
      let needInterceptor: Bool = interceptor()
      // 是否需要拦截
      if needInterceptor {
        NERtcCallKit.sharedInstance().reject(withReason: NSInteger(NERtcCallTerminalCode.TerminalCodeBusy.rawValue))
        return
      }
    }
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEOneOnOneListener else { continue }
      let listener = pointerListener as! NEOneOnOneListener
      if listener
        .responds(to: #selector(NEOneOnOneListener.onOneOnOneInvited(_:userIDs:isFromGroup:groupID:type:attachment:))) {
        listener.onOneOnOneInvited?(invitor, userIDs: userIDs, isFromGroup: isFromGroup, groupID: groupID, type: NEOneOnOneRtcCallType(rawValue: Int(type.rawValue)) ?? .audio, attachment: attachment)
      }
    }
  }
}
