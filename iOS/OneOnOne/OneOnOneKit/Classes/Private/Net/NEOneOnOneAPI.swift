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
    static let reportRtc = NEAPIItem("/nemo/track/rtc-room-created", desc: "创建房间事件上报")
  }

  // 小信AI助手
  enum AIGC {
    static let getOpeningRemark = NEAPIItem("/nemo/openAi/socialChat/getOpeningRemark", desc: "获取开场推荐")
    static let getTopic = NEAPIItem("/nemo/openAi/socialChat/getTopic", desc: "获取话题推荐列表")
    static let topicRecommend = NEAPIItem("/nemo/openAi/socialChat/topicRecommend", desc: "根据话题类型获取AI话题推荐")
    static let chatSkills = NEAPIItem("/nemo/openAi/socialChat/chatSkills", desc: "聊天技巧AI推荐")
    static let chatSkillsWithContext = NEAPIItem("/nemo/openAi/socialChat/chatSkillsWithContext", desc: "聊天技巧-生成一篇文章")
    static let chatReply = NEAPIItem("/nemo/openAi/socialChat/chatReply", desc: "AI聊天回复")
  }
}
