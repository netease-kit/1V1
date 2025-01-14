// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NIMSDK

public class NEOneOnOneMessageUtil {
  /// last message
  /// - Parameter message: message
  /// - Returns: result
  public class func messageContent(message: V2NIMLastMessage) -> NSAttributedString {
    var text = NSAttributedString()
    switch message.messageType {
    case .MESSAGE_TYPE_TEXT:
      if let messageText = message.text {
        text = NSAttributedString(string: messageText)
      }
    case .MESSAGE_TYPE_AUDIO:
      text = NSAttributedString(string: ne_localized("voice"))
    case .MESSAGE_TYPE_IMAGE:
      text = NSAttributedString(string: ne_localized("picture"))
    case .MESSAGE_TYPE_VIDEO:
      text = NSAttributedString(string: ne_localized("video"))
    case .MESSAGE_TYPE_LOCATION:
      text = NSAttributedString(string: ne_localized("location"))
    case .MESSAGE_TYPE_NOTIFICATION:
      text = NSAttributedString(string: ne_localized("notification"))
    case .MESSAGE_TYPE_FILE:
      text = NSAttributedString(string: ne_localized("file"))
    case .MESSAGE_TYPE_TIP:
      if let messageText = message.text {
        text = NSAttributedString(string: messageText)
      }
    case .MESSAGE_TYPE_CALL:
      let record = message.attachment as? V2NIMMessageCallAttachment
      let t = (record?.type == 1) ? ne_localized("internet_phone") :
        ne_localized("video_chat")
      text = NSAttributedString(string: t)
    case .MESSAGE_TYPE_CUSTOM:
      text = NSAttributedString(string: ne_localized("custom"))

      if let messageObject = message.attachment?.raw {
        let attach = CustomAttachment(customJsonSrting: messageObject)
        switch attach.type {
        case OneOnOneChatCustomMessageType.SEND_GIFT_TYPE:
          if message.messageRefer.senderId == NIMSDK.shared().v2LoginService.getLoginUser() {
            text = NSAttributedString(string: ne_localized("gift_message_send"))
          } else {
            let t = NSMutableAttributedString(string: ne_localized("gift_message_recv"))
            text = t
          }
        case OneOnOneChatCustomMessageType.OFFICIAL_GIFT_TYPE:
          if let msg = attach.msg {
            let stringWithoutTags = msg.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression, range: nil)
            text = NSAttributedString(string: stringWithoutTags)
          }
        default:
          text = NSAttributedString(string: ne_localized("custom"))
        }
      }
    default:
      text = NSAttributedString(string: ne_localized("unknown"))
    }

    return text
  }
}
