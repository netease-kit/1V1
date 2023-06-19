// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
/// 透传消息解析
@objcMembers
/// 自定义消息
internal class _NEOneOnOneCustomMessage: NSObject, Codable {
  /// 类型
  var type: Int = -1
  /// data 内容
  var data: _NEOneOnOneCustomMessageData?
}

@objcMembers
/// 自定义消息
internal class _NEOneOnOneCustomMessageData: NSObject, Codable {
  /// 频道号
  var channelId: Int64 = -1
  /// 频道名称
  var channelName: String = ""
  /// 音频数据
  var audio: _NEOneOnOneCustomMessageDataAudio?
  /// 视频数据
  var video: _NEOneOnOneCustomMessageDataVideo?
}

@objcMembers
internal class _NEOneOnOneCustomMessageDataAudio: NSObject, Codable {
  // 违规内容
  var content: String = ""
  // 检测结果，1表示嫌疑，2表示不通过
  var action: Int = -1
  // 违规信息对应的用户ID
  var uid: Int64 = 0
}

@objcMembers
internal class _NEOneOnOneCustomMessageDataVideo: NSObject, Codable {
  // 证据类型，1：图片，2：视频
  var type: Int = 0
  // 证据信息,该字段与审核事件字段保持一致
  var url: String = ""
  // 违规信息对应的用户ID
  var uid: Int64 = 0
}

@objcMembers
internal class _NEOneOnOneAccountInfo: NSObject, Codable {
  // im账号
  var imToken: String = ""
  // 头像
  var icon: String = ""
  // 昵称
  var userName: String = ""
  // 用户id
  var userUuid: String = ""
  // rtcuid
//    var rtcUid:String = ""
  // userToken
  var userToken: String = ""
  // sex
  var sex: Int = 0
  // mobile
  var mobile: String = ""
  var rtcUid: Int?

  var callType: Int?
  // audio 播放地址
  var audioUrl: String?
  // video 播放地址
  var videoUrl: String?
}

@objcMembers
internal class _NEOneOnOneOnlineUserList: NSObject, Codable {
  var data: [_NEOneOnOneOnlineUser]?
}

@objcMembers
internal class _NEOneOnOneOnlineUser: NSObject, Codable {
  // 用户信息
  var userUuid: String?
  // 昵称
  var userName: String?
  // 头像
  var icon: String?

  var mobile: String?
  // tag
  var callType: Int?
  // audio 播放地址
  var audioUrl: String?
  // video 播放地址
  var videoUrl: String?
}
