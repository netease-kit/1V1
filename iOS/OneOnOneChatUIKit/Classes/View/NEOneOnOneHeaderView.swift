// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
class NEOneOnOneHeaderView: UIView {
  var backAction: (() -> Void)?
  var settingAction: (() -> Void)?

  override init(frame: CGRect) {
    super.init(frame: frame)
    loadSubviews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubviews() {
    addSubview(backButton)
    backButton.snp.makeConstraints { make in
      make.left.equalTo(self).offset(12)
      make.width.equalTo(20)
      make.height.equalTo(40)
      make.centerY.equalTo(self)
    }
    addSubview(settingButton)
    settingButton.snp.makeConstraints { make in
      make.centerY.equalTo(backButton)
      make.right.equalTo(self).offset(-20)
      make.width.equalTo(20)
      make.height.equalTo(40)
    }
    /// 顶部视图
    addSubview(navigationItemView)
    navigationItemView.snp.makeConstraints { make in
      make.left.equalTo(self).offset(40)
      make.centerY.equalTo(backButton)
      make.right.equalTo(self).offset(-40)
      make.height.equalTo(45)
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
}
