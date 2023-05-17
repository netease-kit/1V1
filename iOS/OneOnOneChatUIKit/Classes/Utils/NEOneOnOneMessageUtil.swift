// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NIMSDK

public class NEOneOnOneMessageUtil {
  /// last message
  /// - Parameter message: message
  /// - Returns: result
  public class func messageContent(message: NIMMessage) -> NSAttributedString {
    var text = NSAttributedString()
    switch message.messageType {
    case .text:
      if let messageText = message.text {
        text = NSAttributedString(string: messageText)
      }
    case .audio:
      text = NSAttributedString(string: ne_localized("voice"))
    case .image:
      text = NSAttributedString(string: ne_localized("picture"))
    case .video:
      text = NSAttributedString(string: ne_localized("video"))
    case .location:
      text = NSAttributedString(string: ne_localized("location"))
    case .notification:
      text = NSAttributedString(string: ne_localized("notification"))
    case .file:
      text = NSAttributedString(string: ne_localized("file"))
    case .tip:
      if let messageText = message.text {
        text = NSAttributedString(string: messageText)
      }
    case .rtcCallRecord:
      let record = message.messageObject as? NIMRtcCallRecordObject
      let t = (record?.callType == .audio) ? ne_localized("internet_phone") :
        ne_localized("video_chat")
      text = NSAttributedString(string: t)
    case .custom:
      text = NSAttributedString(string: ne_localized("custom"))
      if let messageObject = message.messageObject as? NIMCustomObject,
         let attach = messageObject.attachment as? CustomAttachment {
        switch attach.type {
        case OneOnOneChatCustomMessageType.SEND_GIFT_TYPE:
          if message.isOutgoingMsg {
            text = NSAttributedString(string: ne_localized("gift_message_send"))
          } else {
            let t = NSMutableAttributedString(string: ne_localized("gift_message_recv"))
            if message.status != .read {
              t.addAttribute(.foregroundColor, value: UIColor.red, range: NSRange(location: 0, length: t.length))
            }
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
