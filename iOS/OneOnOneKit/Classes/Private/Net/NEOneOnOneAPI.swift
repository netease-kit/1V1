// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

struct NEAPIItem: NEAPIProtocol {
  let urlPath: String
  var url: String { NE.config.baseUrl + urlPath }
  let description: String
  let extra: String?
  var method: NEHttpMethod
  init(_ url: String,
       desc: String,
       method: NEHttpMethod = .post,
       extra: String? = nil) {
    urlPath = url
    self.method = method
    description = desc
    self.extra = extra
  }
}

enum NEAPI {
  // 1V1模块
  enum OneOnOne {
    static let roomList = NEAPIItem("/nemo/socialChat/user/getOnlineUser", desc: "获取房间列表")
    static let userReport = NEAPIItem("/nemo/socialChat/user/reporter", desc: "心跳上报")
    static let userState = NEAPIItem("/nemo/socialChat/user/getUserState", desc: "获取用户在线状态")
    static let reward = NEAPIItem("/nemo/socialChat/user/reward", desc: "打赏礼物")
    static let accountInfo = NEAPIItem("/nemo/socialChat/user/getUserInfo", desc: "根据userUuid获取账号信息")
    static let loginGetRTCUid = NEAPIItem("/nemo/socialChat/user/login", desc: "登录获取RTCUID")
  }
}
