// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

// 举报入口，非服务端入口，所以临时放在此处

@objcMembers public class NEOneOnOneReportUtil: NSObject {
  public static func report(_ content: String, sessionId: String) {
    if let url = URL(string: "https://statistic.live.126.net/statics/report/common/form") {
      var version = "1.0.0"
      if let projectVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
        version = projectVersion
      }
      var request = URLRequest(url: url)
      request.httpMethod = "POST"
      request.addValue(version, forHTTPHeaderField: "ver")
      request.addValue(NIMSDK.shared().appKey() ?? "", forHTTPHeaderField: "appkey")
      request.addValue("allInOne", forHTTPHeaderField: "sdktype")
      let body = [
        "event": [
          "feedback": [
            "ver": version,
            "os_ver": UIDevice.current.systemVersion,
            "device_id": "device id",
            "description": content,
            "platform": "iOS",
            "manufacturer": "Apple",
            "app_key": NIMSDK.shared().appKey() ?? "",
            "phone": sessionId,
            "nickname": sessionId,
            "client": "allInOne",
            "model": "model",
            "time": Int(Date().timeIntervalSince1970 * 1000),
            "category": "举报",
          ],
        ],
      ]
      /// 请求session
      let sessionConfigure = URLSessionConfiguration.default
      sessionConfigure
        .httpAdditionalHeaders = ["Content-Type": "application/json;charset=utf-8"]
      sessionConfigure.timeoutIntervalForRequest = 10
      sessionConfigure.requestCachePolicy = .reloadIgnoringLocalCacheData
      let session = URLSession(configuration: sessionConfigure)
      if let data = try? JSONSerialization.data(withJSONObject: body, options: []) {
        request.httpBody = data
        let task = session.dataTask(with: request) { data, response, error in
          if let response = response as? HTTPURLResponse,
             response.statusCode == 200 {
            NEOneOnOneToast.show(ne_oneOnOne_localized("举报成功"))
          } else {
            NEOneOnOneToast.show(ne_oneOnOne_localized("举报失败"))
          }
        }
        task.resume()
      }
    }
  }
}
