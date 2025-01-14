// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
protocol NEOneOnOneReciveCustomMessageCallback {
  func onReceiveCustomMessage(_data: [String: Any]?)
}

class NEOneOnOneMessageService: NSObject {
  var callback: NEOneOnOneReciveCustomMessageCallback?
}
