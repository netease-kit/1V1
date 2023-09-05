// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public class NEOneOnOneNavigationItemView: UIView {
  override public init(frame: CGRect) {
    super.init(frame: frame)
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubViews() {
    addSubview(userNameLabel)
    userNameLabel.snp.makeConstraints { make in
      make.centerX.equalTo(self)
      make.centerY.equalTo(self.snp.centerY).offset(-7)
    }

    addSubview(onlineLabel)
    onlineLabel.snp.makeConstraints { make in
      make.centerX.equalTo(self).offset(3)
      make.top.equalTo(userNameLabel.snp.bottom)
    }
    userNameLabel.center = CGPoint(x: center.x, y: center.y + 12)

    addSubview(onlineImageView)
    onlineImageView.snp.makeConstraints { make in
      make.right.equalTo(onlineLabel.snp.left).offset(-3)
      make.centerY.equalTo(onlineLabel)
      make.width.height.equalTo(6)
    }
    addSubview(userInputingLabel)
    userInputingLabel.snp.makeConstraints { make in
      make.centerX.equalTo(userNameLabel)
      make.top.equalTo(userNameLabel.snp.bottom)
    }
    userInputingLabel.isHidden = true
  }

  lazy var userNameLabel: UILabel = {
    let userNameLabel = UILabel()
    userNameLabel.font = UIFont(name: "PingFangSC-Medium", size: 17)
    userNameLabel.textColor = UIColor(hexString: "#222222")
    userNameLabel.textAlignment = .center
    userNameLabel.text = ne_localized("用户")
    userNameLabel.accessibilityIdentifier = "id.tvTitle"
    return userNameLabel
  }()

  lazy var onlineLabel: UILabel = {
    let onlineLabel = UILabel()
    onlineLabel.font = UIFont(name: "PingFangSC-Regular", size: 12)
    onlineLabel.textColor = UIColor(hexString: "#828798")
    onlineLabel.text = ne_localized("在线")
    onlineLabel.isHidden = true
    onlineLabel.accessibilityIdentifier = "id.tvOnline"
    return onlineLabel
  }()

  lazy var userInputingLabel: UILabel = {
    let userInputingLabel = UILabel()
    userInputingLabel.text = ne_localized("对方正在输入中...")
    userInputingLabel.textColor = UIColor(hexString: "#828798")
    userInputingLabel.textAlignment = .center
    userInputingLabel.font = UIFont(name: "PingFangSC-Regular", size: 12)
    return userInputingLabel
  }()

  lazy var onlineImageView: UIImageView = {
    let onlineImageView = UIImageView()
    onlineImageView.image = ne_chatUI_imageName(imageName: "online_green_icon")
    onlineImageView.isHidden = true
    return onlineImageView
  }()
}
