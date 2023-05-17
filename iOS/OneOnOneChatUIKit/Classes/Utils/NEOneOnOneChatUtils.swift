// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import UIKit

class NEOneOnOneChatUtil: NSObject {
  class func ne_localBundle() -> Bundle {
    Bundle(for: NEOneOnOneChatUtil.self)
  }
}

func ne_localized(_ key: String) -> String {
  NEOneOnOneChatUtil.ne_localBundle().localizedString(forKey: key, value: nil, table: "Localizable")
}

func ne_chatUI_imageName(imageName: String) -> UIImage? {
  if #available(iOS 13.0, *) {
    return UIImage(named: imageName, in: NEOneOnOneChatUtil.ne_localBundle(), with: nil)
  } else {
    return UIImage(named: imageName, in: NEOneOnOneChatUtil.ne_localBundle(), compatibleWith: nil)
  }
}
