// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objc
/// 通话类型
public enum NEOneOnOneRtcCallType: Int {
  /// 音频
  case audio = 1
  /// 视频
  case video
}

@objc

/// 在忙类型
public enum NEOneOnOneRejectReason: Int {
  /// 在语聊房
  case reject_by_voiceRoom = 3000
  /// 在一起听
  case reject_by_listenTogether
}

@objcMembers
public class NEOneOnOneUser: NSObject, Codable {
  /// 用户系统手机号
  public var mobile: String = ""
  /// 用户头像
  public var avatar: String = ""
  /// 云信IM 账号ID
  public var imAccid: String = ""
  /// 云信IM token
  public var imToken: String = ""
  /// 用户系统登录token
  public var accessToken: String = ""
  /// 音视频房间ID
  public var avRoomUid: String = ""
  // 主叫
  public var isCaller: Bool = false

  public var nickName: String = ""
  override public init() {
    super.init()
  }
}

@objcMembers
/// 自定义消息
public class NEOneOnOneCustomMessage: NSObject {
  /// 类型
  public var type: Int = -1
  /// data 内容
  public var data: NEOneOnOneCustomMessageData?

  init(data: _NEOneOnOneCustomMessage? = nil) {
    if let data = data {
      type = data.type
      self.data = NEOneOnOneCustomMessageData(data.data)
    }
  }
}

@objcMembers
/// 自定义消息
public class NEOneOnOneCustomMessageData: NSObject {
  /// 频道号
  public var channelId: Int64 = -1
  /// 频道名称
  public var channelName: String = ""
  /// 音频数据
  public var audio: NEOneOnOneCustomMessageDataAudio?
  /// 视频数据
  public var video: NEOneOnOneCustomMessageDataVideo?

  override public init() {
    super.init()
  }

  init(_ messageData: _NEOneOnOneCustomMessageData?) {
    if let messageData = messageData {
      channelId = messageData.channelId
      channelName = messageData.channelName
      audio = NEOneOnOneCustomMessageDataAudio(messageData.audio)
      video = NEOneOnOneCustomMessageDataVideo(messageData.video)
    }
  }
}

@objcMembers
public class NEOneOnOneCustomMessageDataAudio: NSObject {
  // 违规内容
  public var content: String = ""
  // 检测结果，1表示嫌疑，2表示不通过
  public var action: Int = -1
  // 违规信息对应的用户ID
  public var uid: Int64 = 0
  init(_ audioData: _NEOneOnOneCustomMessageDataAudio?) {
    if let audioData = audioData {
      content = audioData.content
      action = audioData.action
      uid = audioData.uid
    }
  }
}

@objcMembers
public class NEOneOnOneCustomMessageDataVideo: NSObject {
  // 证据类型，1：图片，2：视频
  public var type: Int = 0
  // 证据信息,该字段与审核事件字段保持一致
  public var url: String = ""
  // 违规信息对应的用户ID
  public var uid: Int64 = 0

  init(_ videoData: _NEOneOnOneCustomMessageDataVideo?) {
    if let videoData = videoData {
      type = videoData.type
      url = videoData.url
      uid = videoData.uid
    }
  }
}

@objcMembers
public class NEOneOnOneAccountInfo: NSObject {
  // 手机号
  public var mobile: String = ""
  // im账号
  public var imAccid: String = ""

  public var avatar: String = ""
  // 昵称
  public var nickname: String = ""
  init(_ create: _NEOneOnOneAccountInfo?) {
    if let create = create {
      mobile = create.mobile
      imAccid = create.imAccid
      avatar = create.avatar
      nickname = create.nickname
    }
  }
}

@objcMembers
public class NEOneOnOneOnlineUserList: NSObject {
  public var data: [NEOneOnOneOnlineUser]?
  internal init(_ create: _NEOneOnOneOnlineUserList? = nil) {
    if let datas = create?.data {
      data = datas.map { _onlineUser in
        NEOneOnOneOnlineUser(_onlineUser)
      }
    }
  }
}

@objcMembers
public class NEOneOnOneOnlineUser: NSObject {
  // 用户信息
  public var accountId: String?
  // 昵称
  public var userName: String?
  // 头像
  public var icon: String?
  // imaccid
  public var mobile: String?

  override public init() {
    super.init()
  }

  internal init(_ create: _NEOneOnOneOnlineUser?) {
    if let create = create {
      accountId = create.accountId
      userName = create.userName
      icon = create.icon
      mobile = create.mobile
    }
  }
}
