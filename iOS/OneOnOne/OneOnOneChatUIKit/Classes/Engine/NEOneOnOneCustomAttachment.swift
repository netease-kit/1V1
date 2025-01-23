
// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NEChatUIKit
import NEOneOnOneKit
import NIMSDK
import UIKit

public class CustomAttachment: NSObject {
  // UI展示cell的类型
  public var customType: Int = 0

  public var cellHeight: CGFloat = 0

  // 业务类型
  public var type: Int = 0

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

  // 小秘书消息
  public var msg: String?

  public func encode() -> String {
    let info = [
      "type": type,
      "senderUserUuid": senderUserUuid ?? "",
      "sendTime": sendTime,
      "rewarderUserName": rewarderUserName ?? "",
      "giftCount": giftCount,
      "giftId": giftId,
      "targetUserUuid": targetUserUuid ?? "",
      "targetUserName": targetUserName ?? "",
      "msg": msg ?? "",
      "customHeight": cellHeight,

    ] as [String: Any]

    let jsonData = try? JSONSerialization.data(withJSONObject: info, options: .prettyPrinted)
    var content = ""
    if let data = jsonData {
      content = String(data: data, encoding: .utf8) ?? ""
    }
    return content
  }

  init(customJsonSrting: String) {
    // 将JSON字符串转换为Data
    if let jsonData = customJsonSrting.data(using: .utf8) {
      do {
        // 反序列化JSON数据为字典
        if let dictionary = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
          let data = dictionary["data"] as? [String: Any] ?? dictionary
          type = data["type"] as? Int ?? 0
          senderUserUuid = data["senderUserUuid"] as? String ?? ""
          sendTime = data["sendTime"] as? Int ?? 0
          rewarderUserName = data["rewarderUserName"] as? String ?? ""
          giftId = data["giftId"] as? Int ?? 0
          giftCount = data["giftCount"] as? Int ?? 0
          targetUserUuid = data["targetUserUuid"] as? String ?? ""
          targetUserName = data["targetUserName"] as? String ?? ""
          msg = data["msg"] as? String
        }
      } catch {
        print("Error: \(error.localizedDescription)")
      }
    }
  }
}

// public class CustomAttachmentDecoder: NSObject, NIMCustomAttachmentCoding {
//  public func decodeAttachment(_ content: String?) -> NIMCustomAttachment? {
//    var attachment: NIMCustomAttachment?
//    if let data = content?.data(using: .utf8),
//       let infoDict = try? JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [String: Any],
//       let type = infoDict["type"] as? Int,
//       (OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TIPS_TYPE ... OneOnOneChatCustomMessageType.OFFICIAL_GIFT_TYPE).contains(type) {
//      if let data = infoDict["data"] as? [String: Any] {
//        attachment = decodeCustomMessage(info: data, type: type)
//      } else {
//        attachment = decodeCustomMessage(info: infoDict, type: type)
//      }
//    }
//    return attachment
//  }
//
//  func decodeCustomMessage(info: [String: Any], type: Int) -> CustomAttachment {
//    let customAttachment = CustomAttachment()
//    customAttachment.type = type
//    customAttachment.customType = type
//    if let senderUserUuid = info["senderUserUuid"] as? String {
//      customAttachment.senderUserUuid = senderUserUuid
//    }
//    if let sendTime = info["sendTime"] as? Int {
//      customAttachment.sendTime = sendTime
//    }
//    if let rewarderUserName = info["rewarderUserName"] as? String {
//      customAttachment.rewarderUserName = rewarderUserName
//    }
//    if let giftId = info["giftId"] as? Int {
//      customAttachment.giftId = giftId
//    }
//    if let giftCount = info["giftCount"] as? Int {
//      customAttachment.giftCount = giftCount
//    }
//    if let targetUserUuid = info["targetUserUuid"] as? String {
//      customAttachment.targetUserUuid = targetUserUuid
//    }
//    if let targetUserName = info["targetUserName"] as? String {
//      customAttachment.targetUserName = targetUserName
//    }
//    if let msg = info["msg"] as? String {
//      customAttachment.msg = msg
//    }
//    switch customAttachment.type {
//    case OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TIPS_TYPE:
//      customAttachment.cellHeight = 30 + 20
//    case OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE, OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE,
//         OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE,
//         OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE:
//      customAttachment.cellHeight = 50 + 20
//    case OneOnOneChatCustomMessageType.SEND_GIFT_TYPE:
//      if let senderAccount = customAttachment.senderUserUuid {
//        if senderAccount == NEOneOnOneKit.getInstance().localMember?.imAccid {
//          customAttachment.customType = SEND_GIFT_TYPE_SEND
//        } else {
//          customAttachment.customType = SEND_GIFT_TYPE_RECV
//        }
//        customAttachment.cellHeight = 114
//      }
//    case OneOnOneChatCustomMessageType.OFFICIAL_GIFT_TYPE:
//      // 预留12作为下边距
//      customAttachment.cellHeight = NEOneOnOneOfficialSalution.getSize(content: customAttachment.msg ?? "").height + 12
//    default:
//      customAttachment.cellHeight = 50 + 20
//    }
//    return customAttachment
//  }
// }
