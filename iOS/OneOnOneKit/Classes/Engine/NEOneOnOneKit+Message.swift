// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NIMSDK

enum NEOneOnOneMsgType: RawRepresentable {
  typealias RawValue = Int

  /// 未知
  case unknow
  // 400表示实时音视频安全通审核，401表示云端录制安全通审核
  /// 自定义透传消息
  case audioOrVideoRealTimePassThroughMessage
  case audioOrVideoCloudPassThroughMessage
  case audioOrVideoNormal

  init?(rawValue: Int) {
    if rawValue == 400 {
      self = .audioOrVideoRealTimePassThroughMessage
    } else if rawValue == 401 {
      self = .audioOrVideoCloudPassThroughMessage
    } else if rawValue == 3000 {
      self = .audioOrVideoNormal
    } else {
      self = .unknow
    }
  }

  var rawValue: RawValue {
    var num: Int
    switch self {
    case .audioOrVideoRealTimePassThroughMessage: num = 400
    case .audioOrVideoCloudPassThroughMessage: num = 401
    case .audioOrVideoNormal: num = 3000
    default: num = 0
    }
    return num
  }

  func describe() -> String {
    switch self {
    case .audioOrVideoRealTimePassThroughMessage: return "400【Audio Video Real Time Pass throught message】"
    case .audioOrVideoCloudPassThroughMessage: return "401【Audio Video Cloud Pass throught message】"
    case .audioOrVideoNormal: return "3000【Audio Video Normal throught message】"
    default: return "【Unknown】"
    }
  }
}

extension NEOneOnOneKit: NIMPassThroughManagerDelegate {
  public func didReceivedPassThroughMsg(_ recvData: NIMPassThroughMsgData?) {
    guard let body = recvData?.body,
          let dict = try? JSONSerialization.jsonObject(
            with: body.data(using: String.Encoding.utf8)!,
            options: .mutableContainers
          ) as? [String: Any] else { return }

    guard let cmd = dict["type"] as? Int else { return }

    let msgType = NEOneOnOneMsgType(rawValue: cmd) ?? .unknow

    var audioOrVideo = false
    switch msgType {
    case .audioOrVideoRealTimePassThroughMessage, .audioOrVideoCloudPassThroughMessage, .audioOrVideoNormal:
      audioOrVideo = true
    default: break
    }
    /// 透传 处理
    guard audioOrVideo else {
      return
    }

    NEOneOnOneLog.infoLog(kitTag, desc: "✉️  \(msgType.describe())\n\(dict.prettyJSON)")

    let passThrough = NEOneOnOneDecoder.decode(_NEOneOnOneCustomMessage.self, param: dict)

    notifityCustomMsg(message: passThrough)
  }

  private func notifityCustomMsg(message: _NEOneOnOneCustomMessage?) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEOneOnOneListener else { continue }
      let listener = pointerListener as! NEOneOnOneListener
      if listener
        .responds(to: #selector(NEOneOnOneListener.onReceiveCustomMessage(message:))) {
        listener.onReceiveCustomMessage?(message: NEOneOnOneCustomMessage(data: message))
      }
    }
  }

  private func notifityNotificationCustomMsg(message: _NEOneOnOneCustomMessage?) {
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEOneOnOneListener else { continue }
      let listener = pointerListener as! NEOneOnOneListener
      if listener
        .responds(to: #selector(NEOneOnOneListener.onReceiveNotificationCustomMessage(message:))) {
        listener.onReceiveNotificationCustomMessage?(message: NEOneOnOneCustomMessage(data: message))
      }
    }
  }
}

extension NEOneOnOneKit: NIMSystemNotificationManagerDelegate {
  public func onReceive(_ notification: NIMCustomSystemNotification) {
    NEOneOnOneLog.infoLog(
      kitTag,
      desc: #function + ", notification.description:" + notification.description
    )

    guard let body = notification.content,
          let dict = try? JSONSerialization.jsonObject(
            with: body.data(using: String.Encoding.utf8)!,
            options: .mutableContainers
          ) as? [String: Any] else { return }

    guard let cmd = dict["type"] as? Int else { return }

    let msgType = NEOneOnOneMsgType(rawValue: cmd) ?? .unknow

    var audioOrVideo = false
    switch msgType {
    case .audioOrVideoRealTimePassThroughMessage, .audioOrVideoCloudPassThroughMessage, .audioOrVideoNormal:
      audioOrVideo = true
    default: break
    }
    /// 透传 处理
    guard audioOrVideo else {
      return
    }

    NEOneOnOneLog.infoLog(kitTag, desc: "✉️  \(msgType.describe())\n\(dict.prettyJSON)")

    let passThrough = NEOneOnOneDecoder.decode(_NEOneOnOneCustomMessage.self, param: dict)

    notifityNotificationCustomMsg(message: passThrough)
  }
}

extension NEOneOnOneKit: NIMChatManagerDelegate {
  public func onRecvMessages(_ messages: [NIMMessage]) {
    for msg in messages {
      if let rawAttachContent = msg.rawAttachContent,
         let data = rawAttachContent.data(using: .utf8),
         let infoDict = try? JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [String: Any],
         let type = infoDict["type"] as? Int,
         type == 1004,
         let data = infoDict["data"] as? [String: Any] {
        let gift = NEOneOnOneOneGift()
        gift.senderUserUuid = data["senderUserUuid"] as? String
        gift.rewarderUserName = data["rewarderUserName"] as? String
        gift.targetUserUuid = data["targetUserUuid"] as? String
        gift.targetUserName = data["targetUserName"] as? String
        gift.sendTime = data["sendTime"] as? Int ?? 0
        gift.giftCount = data["giftCount"] as? Int ?? 0
        gift.giftId = data["giftId"] as? Int ?? 0
        for pointerListener in listeners.allObjects {
          if let listener = pointerListener as? NEOneOnOneListener,
             listener.responds(to: #selector(NEOneOnOneListener.onReceiveGift(gift:))) {
            listener.onReceiveGift?(gift: gift)
          }
        }
      }
    }
  }
}
