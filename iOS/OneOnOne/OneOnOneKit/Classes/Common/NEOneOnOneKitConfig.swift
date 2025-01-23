// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// OneOnOneKit 配置项
@objcMembers
public class NEOneOnOneKitConfig: NSObject {
  /// appKey 为OneOnOne服务的Key
  public var appKey: String = ""
  public var APNSCerName: String = ""
  public var VoIPCerName: String?
  /// 预留字段
  public var extras: [String: String] = .init()
}
