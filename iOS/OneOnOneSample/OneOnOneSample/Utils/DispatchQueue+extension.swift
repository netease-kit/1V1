// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public extension DispatchQueue {
  private static var _onceTracker = [String]()

  class func once(file: String = #file, function: String = #function, line: Int = #line, block: () -> Void) {
    let token = "\(file):\(function):\(line)"
    once(token: token, block: block)
  }

  class func once(token: String, block: () -> Void) {
    objc_sync_enter(self)
    defer {
      objc_sync_exit(self)
    }
    guard !_onceTracker.contains(token) else { return }
    _onceTracker.append(token)
    block()
  }
}
