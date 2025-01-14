// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

/// 通话类型
@objc
public enum NEOneOnOneRtcCallType: Int {
  /// 音频
  case audio = 1
  /// 视频
  case video
}

/// 在忙类型
@objc

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
  // RTCUid
  public var rtcUid: Int = 0
  override public init() {
    super.init()
  }
}

/// 自定义消息
@objcMembers
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

/// 自定义消息
@objcMembers
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
  init?(_ audioData: _NEOneOnOneCustomMessageDataAudio?) {
    guard let audioData = audioData else { return nil }
    content = audioData.content
    action = audioData.action
    uid = audioData.uid
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

  init?(_ videoData: _NEOneOnOneCustomMessageDataVideo?) {
    guard let videoData = videoData else { return nil }
    type = videoData.type
    url = videoData.url
    uid = videoData.uid
  }
}

@objcMembers
public class NEOneOnOneAccountInfo: NSObject {
  // im账号
  public var imToken: String = ""
  // 头像
  public var icon: String = ""
  // 昵称
  public var userName: String = ""
  // 用户id
  public var userUuid: String = ""
  // rtcuid
//    public var rtcUid:String = ""
  // userToken
  public var userToken: String = ""
  // mobile
  public var mobile: String = ""
  // sex
  public var sex: Int = 0

  public var rtcUid: Int?

  public var callType: Int?
  // audio 播放地址
  public var audioUrl: String?
  // video 播放地址
  public var videoUrl: String?

  public var oc_callType: Int {
    set {
      callType = newValue
    }
    get {
      callType ?? 0
    }
  }

  init(_ create: _NEOneOnOneAccountInfo?) {
    if let create = create {
      imToken = create.imToken
      icon = create.icon
      userName = create.userName
      userUuid = create.userUuid
//            self.rtcUid = create.rtcUid
      userToken = create.userToken
      sex = create.sex
      mobile = create.mobile
      rtcUid = create.rtcUid
      callType = create.callType
      audioUrl = create.audioUrl
      videoUrl = create.videoUrl
    }
  }
}

@objcMembers
public class NEOneOnOneOnlineUserList: NSObject {
  public var data: [NEOneOnOneOnlineUser]?
  init(_ create: _NEOneOnOneOnlineUserList? = nil) {
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
  public var userUuid: String?
  // 昵称
  public var userName: String?
  // 头像
  public var icon: String?
  // imaccid
  public var mobile: String?

  public var callType: Int?
  // audio 播放地址
  public var audioUrl: String?
  // video 播放地址
  public var videoUrl: String?

  public var oc_callType: Int {
    set {
      callType = newValue
    }
    get {
      callType ?? 0
    }
  }

  override public init() {
    super.init()
  }

  init(_ create: _NEOneOnOneOnlineUser?) {
    if let create = create {
      userUuid = create.userUuid
      userName = create.userName
      icon = create.icon
      mobile = create.mobile
      callType = create.callType
      audioUrl = create.audioUrl
      videoUrl = create.videoUrl
    }
  }
}

@objcMembers
public class NEOneOnOneOneGift: NSObject {
  // 消息发送者用户编号
  public var senderUserUuid: String?
  // 打赏者昵称
  public var rewarderUserName: String?
  // 发送消息时间
  public var sendTime: Int = 0
  // 礼物个数
  public var giftCount: Int = 0
  // 礼物编号
  public var giftId: Int = 0
  // 打赏者用户编号
  public var targetUserUuid: String?
  // 打赏者昵称
  public var targetUserName: String?
}
