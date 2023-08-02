// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers public class NEOneOnOneAIGCTopic: NSObject, Codable {
  /// 话题类型
  public var topicType: Int = 0

  /// 话题描述
  public var desc: String?
}

public extension NEOneOnOneKit {
  /// 获取开场推荐
  /// - Parameter callback: 回调
  func getOpeningRemark(callback: NEOneOnOneCallback<[String]>? = nil) {
    let params: [String: Any] = [
      "next": false,
    ]
    NEAPI.AIGC.getOpeningRemark.request(params) { dic in
      if let dic = dic,
         let data = dic["data"] as? [String] {
        callback?(NEOneOnOneErrorCode.success, nil, data)
      } else {
        callback?(NEOneOnOneErrorCode.failed, nil, nil)
      }
    } failed: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }

  /// 获取话题推荐列表
  /// - Parameter callback: 回调
  func getTopic(callback: NEOneOnOneCallback<[NEOneOnOneAIGCTopic]>? = nil) {
    NEAPI.AIGC.getTopic.request { dic in
      if let dic = dic,
         let data = dic["data"] as? [[String: Any]] {
        let list: [NEOneOnOneAIGCTopic] = data.compactMap { map in
          let topic = NEOneOnOneAIGCTopic()
          topic.topicType = (map["topicType"] as? Int) ?? 1
          topic.desc = map["desc"] as? String
          return topic
        }
        callback?(NEOneOnOneErrorCode.success, nil, list)
      } else {
        callback?(NEOneOnOneErrorCode.failed, nil, nil)
      }
    } failed: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }

  /// 根据话题类型获取AI话题推荐
  /// - Parameters:
  ///   - topicType: 话题类型
  ///   - callback: 回调
  func topicRecommend(topicType: Int, callback: NEOneOnOneCallback<[String]>? = nil) {
    let params: [String: Any] = [
      "next": false,
      "topicType": topicType,
    ]
    NEAPI.AIGC.topicRecommend.request(params) { dic in
      if let dic = dic,
         let data = dic["data"] as? [String] {
        callback?(NEOneOnOneErrorCode.success, nil, data)
      } else {
        callback?(NEOneOnOneErrorCode.failed, nil, nil)
      }
    } failed: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }

  /// AI推荐聊天技巧
  /// - Parameter callback: 回调
  func chatSkills(callback: NEOneOnOneCallback<[String]>? = nil) {
    NEAPI.AIGC.chatSkills.request { dic in
      if let dic = dic,
         let data = dic["data"] as? [String] {
        callback?(NEOneOnOneErrorCode.success, nil, data)
      } else {
        callback?(NEOneOnOneErrorCode.failed, nil, nil)
      }
    } failed: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }

  /// 根据关键字生成一篇文章
  /// - Parameters:
  ///   - context: 关键字
  ///   - callback: 回调
  func chatSkillsWithContext(_ context: String, callback: NEOneOnOneCallback<String>? = nil) {
    let params: [String: Any] = [
      "context": context,
    ]
    NEAPI.AIGC.chatSkillsWithContext.request(params) { dic in
      if let dic = dic,
         let data = dic["data"] as? String {
        callback?(NEOneOnOneErrorCode.success, nil, data)
      } else {
        callback?(NEOneOnOneErrorCode.failed, nil, nil)
      }
    } failed: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }

  /// 根据对方消息信息，返回AI回复推荐信息
  /// - Parameters:
  ///   - msg: 对方的消息内容
  ///   - callback: 回调
  func chatReply(msg: String, callback: NEOneOnOneCallback<[String]>? = nil) {
    let params: [String: Any] = [
      "msg": msg,
      "next": false,
    ]
    NEAPI.AIGC.chatReply.request(params) { dic in
      if let dic = dic,
         let data = dic["data"] as? [String] {
        callback?(NEOneOnOneErrorCode.success, nil, data)
      } else {
        callback?(NEOneOnOneErrorCode.failed, nil, nil)
      }
    } failed: { error in
      callback?(error.code, error.localizedDescription, nil)
    }
  }
}
