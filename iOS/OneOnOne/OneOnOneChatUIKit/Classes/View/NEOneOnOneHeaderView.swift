// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
class NEOneOnOneHeaderView: UIView {
  var backAction: (() -> Void)?
  var settingAction: (() -> Void)?

  public init(frame: CGRect, showTips: Bool) {
    super.init(frame: frame)
    loadSubviews(showTips: showTips)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubviews(showTips: Bool) {
    addSubview(backButton)
    backButton.snp.makeConstraints { make in
      make.left.equalTo(self).offset(12)
      make.width.equalTo(20)
      make.height.equalTo(40)
      make.top.equalTo(self)
    }
    addSubview(settingButton)
    settingButton.snp.makeConstraints { make in
      make.top.equalTo(backButton)
      make.right.equalTo(self).offset(-20)
      make.width.equalTo(20)
      make.height.equalTo(40)
    }
    /// 顶部视图
    addSubview(navigationItemView)
    navigationItemView.snp.makeConstraints { make in
      make.left.equalTo(self).offset(40)
      make.top.equalTo(backButton)
      make.right.equalTo(self).offset(-40)
      make.height.equalTo(45)
    }

    if showTips {
      addSubview(virtualTipsView)
      virtualTipsView.snp.makeConstraints { make in
        make.left.right.bottom.equalToSuperview()
        make.height.equalTo(36)
      }
    }
  }

  lazy var navigationItemView: NEOneOnOneNavigationItemView = {
    let navigationItemView = NEOneOnOneNavigationItemView()
    return navigationItemView
  }()

  lazy var backButton: UIButton = {
    let backButton = UIButton()
    backButton.setImage(ne_chatUI_imageName(imageName: "header_back_icon"), for: .normal)
    backButton.addTarget(self, action: #selector(clickBackButton), for: .touchUpInside)
    return backButton
  }()

  lazy var settingButton: UIButton = {
    let settingButton = UIButton()
    settingButton.setImage(ne_chatUI_imageName(imageName: "threePoint"), for: .normal)
//        settingButton.setBackgroundImage(ne_chatUI_imageName(imageName: "header_back_icon"), for: .normal)
    settingButton.addTarget(self, action: #selector(clickSettingButton), for: .touchUpInside)
    settingButton.accessibilityIdentifier = "id.ivSetting"
    return settingButton
  }()

  @objc func clickBackButton() {
    if let backAction = backAction {
      backAction()
    }
  }

  @objc func clickSettingButton() {
    if let settingAction = settingAction {
      settingAction()
    }
  }

  lazy var virtualTipsView: UILabel = {
    let label = UILabel()
    label.backgroundColor = UIColor(red: 254 / 255, green: 227 / 255, blue: 230 / 255, alpha: 1)
    label.textAlignment = .center
    let font = UIFont.systemFont(ofSize: 14)
    var mutableString = NSMutableAttributedString()
    if let image = ne_chatUI_imageName(imageName: "tips") {
      let attachment = NSTextAttachment()
      attachment.image = image
      attachment.bounds = CGRect(x: 0, y: (font.capHeight - 16) / 2, width: 16, height: 16)
      mutableString.append(NSAttributedString(attachment: attachment))
    }
    mutableString.append(NSAttributedString(string: " \(ne_localized("当前账号仅用于功能测试（限制10次）详情请咨询官网"))", attributes: [.font: font, .foregroundColor: UIColor(red: 252 / 255, green: 89 / 255, blue: 106 / 255, alpha: 1)]))
    label.attributedText = mutableString
    return label
  }()
}
