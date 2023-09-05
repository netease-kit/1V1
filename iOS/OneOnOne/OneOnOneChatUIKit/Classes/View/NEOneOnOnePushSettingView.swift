// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import SnapKit
import UIKit

@objc protocol NEOneOnOnePushSettingViewDelegate: NSObjectProtocol {
  func settingViewToStartPush(_ view: NEOneOnOnePushSettingView)
  func settingViewClose(_ view: NEOneOnOnePushSettingView)
}

class NEOneOnOnePushSettingView: UIView {
  weak var delegate: NEOneOnOnePushSettingViewDelegate?

  override public init(frame: CGRect) {
    super.init(frame: frame)
    setUpSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func setUpSubViews() {
    backgroundColor = UIColor(hexString: "#FDF5F3")
    addSubview(contentLabel)
    addSubview(settingBtn)
    addSubview(closeBtn)

    closeBtn.snp.makeConstraints { make in
      make.centerY.equalToSuperview()
      make.right.equalToSuperview().offset(-12)
      make.width.height.equalTo(16)
    }

    settingBtn.snp.makeConstraints { make in
      make.centerY.equalToSuperview()
      make.right.equalTo(closeBtn.snp.left).offset(-12)
      make.width.equalTo(64)
      make.height.equalTo(24)
    }

    contentLabel.snp.makeConstraints { make in
      make.top.bottom.equalToSuperview()
      make.left.equalToSuperview().offset(18)
      make.right.equalTo(closeBtn.snp.left)
    }
  }

  lazy var contentLabel: UILabel = {
    var view = UILabel()
    view.frame = CGRect(x: 0, y: 0, width: 143, height: 18)
    view.backgroundColor = .clear
    view.textColor = UIColor(red: 0.294, green: 0.286, blue: 0.282, alpha: 1)
    view.font = UIFont(name: "PingFangSC-Regular", size: 13)
    view.text = ne_localized("打开推送，及时接收消息")
    return view
  }()

  lazy var settingBtn: UIButton = {
    var view = UIButton()
    view.frame = CGRect(x: 0, y: 0, width: 64, height: 24)
    view.backgroundColor = UIColor(red: 0.91, green: 0.227, blue: 0.224, alpha: 1)
    view.layer.cornerRadius = 12
    view.clipsToBounds = true
    view.setTitle(ne_localized("立即开启"), for: .normal)
    view.setTitleColor(.white, for: .normal)
    view.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 11) ?? UIFont.systemFont(ofSize: 11)
    view.addTarget(delegate, action: #selector(NEOneOnOnePushSettingViewDelegate.settingViewToStartPush(_:)), for: .touchUpInside)
    return view
  }()

  lazy var closeBtn: UIButton = {
    var view = UIButton()
    view.frame = CGRect(x: 0, y: 0, width: 16, height: 16)
    view.setImage(ne_chatUI_imageName(imageName: "close"), for: .normal)
    view.addTarget(delegate, action: #selector(NEOneOnOnePushSettingViewDelegate.settingViewClose(_:)), for: .touchUpInside)
    return view
  }()
}
