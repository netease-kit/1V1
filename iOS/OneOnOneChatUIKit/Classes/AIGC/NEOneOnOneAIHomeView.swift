// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import SnapKit

protocol NEOneOnOneAIHomeDelegate: AnyObject {
  func homeBegin()
  func homeTopic()
  func homeSkill()
  func homeReplay()
}

class NEOneOnOneAIHomeView: UIView {
  weak var delegate: NEOneOnOneAIHomeDelegate?

  override init(frame: CGRect) {
    super.init(frame: frame)

    backgroundColor = .white
    if #available(iOS 11.0, *) {
      layer.cornerRadius = 16
      layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
    }

    backgroundColor = UIColor(hexString: "eef5fe")

    addSubview(icon)
    addSubview(nameLabel)
    addSubview(tipsLabel)
    addSubview(beginButton)
    addSubview(topicButton)
    addSubview(skillButton)
    addSubview(replayButton)

    icon.snp.makeConstraints { make in
      make.width.height.equalTo(48)
      make.left.top.equalToSuperview().offset(20)
    }

    nameLabel.snp.makeConstraints { make in
      make.left.equalTo(icon.snp.right).offset(15)
      make.top.equalTo(icon)
      make.right.equalToSuperview()
    }

    tipsLabel.snp.makeConstraints { make in
      make.left.equalTo(nameLabel)
      make.right.equalToSuperview()
      make.top.equalTo(nameLabel.snp.bottom).offset(2)
    }

    let margin: CGFloat = 20
    let width: CGFloat = (frame.width - 5 * margin) / 4
    beginButton.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(margin)
      make.width.equalTo(width)
      make.top.equalTo(icon.snp.bottom).offset(30)
      if #available(iOS 11.0, *) {
        make.bottom.equalTo(safeAreaLayoutGuide).offset(-60)
      } else {
        make.bottom.equalToSuperview().offset(-60)
      }
    }

    topicButton.snp.makeConstraints { make in
      make.left.equalTo(beginButton.snp.right).offset(margin)
      make.width.equalTo(width)
      make.top.bottom.equalTo(beginButton)
    }

    skillButton.snp.makeConstraints { make in
      make.left.equalTo(topicButton.snp.right).offset(margin)
      make.width.equalTo(width)
      make.top.bottom.equalTo(beginButton)
    }

    replayButton.snp.makeConstraints { make in
      make.left.equalTo(skillButton.snp.right).offset(margin)
      make.width.equalTo(width)
      make.top.bottom.equalTo(beginButton)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var icon: UIImageView = {
    let view = UIImageView(image: ne_chatUI_imageName(imageName: "ai_rebort"))
    return view
  }()

  lazy var nameLabel: UILabel = {
    let view = UILabel()
    view.text = ne_localized("AI畅聊") + "(Beta)"
    view.textColor = UIColor(red: 0.133, green: 0.133, blue: 0.133, alpha: 1)
    view.font = UIFont(name: "PingFangSC-Medium", size: 16)
    return view
  }()

  lazy var tipsLabel: UILabel = {
    let view = UILabel()
    view.text = ne_localized("天涯若比邻，和Ta开启好友畅聊之旅")
    view.textColor = UIColor(red: 0.6, green: 0.6, blue: 0.6, alpha: 1)
    view.font = UIFont(name: "PingFangSC-Regular", size: 12)
    return view
  }()

  lazy var beginButton: NEOneOnOneImageButton = {
    let button = NEOneOnOneImageButton(frame: CGRect(x: 0, y: 0, width: 75, height: 75))
    button.setTitle(ne_localized("开场话术"), for: .normal)
    button.setImage(ne_chatUI_imageName(imageName: "ai_begin"), for: .normal)
    button.addTarget(self, action: #selector(buttonClicked(sender:)), for: .touchUpInside)
    return button
  }()

  lazy var topicButton: NEOneOnOneImageButton = {
    let button = NEOneOnOneImageButton(frame: CGRect(x: 0, y: 0, width: 75, height: 75))
    button.setTitle(ne_localized("话题推荐"), for: .normal)
    button.setImage(ne_chatUI_imageName(imageName: "ai_topic"), for: .normal)
    button.addTarget(self, action: #selector(buttonClicked(sender:)), for: .touchUpInside)
    return button
  }()

  lazy var skillButton: NEOneOnOneImageButton = {
    let button = NEOneOnOneImageButton(frame: CGRect(x: 0, y: 0, width: 75, height: 75))
    button.setTitle(ne_localized("聊天技巧"), for: .normal)
    button.setImage(ne_chatUI_imageName(imageName: "ai_skill"), for: .normal)
    button.addTarget(self, action: #selector(buttonClicked(sender:)), for: .touchUpInside)
    return button
  }()

  lazy var replayButton: NEOneOnOneImageButton = {
    let button = NEOneOnOneImageButton(frame: CGRect(x: 0, y: 0, width: 75, height: 75))
    button.setTitle(ne_localized("对话回复"), for: .normal)
    button.setImage(ne_chatUI_imageName(imageName: "ai_replay"), for: .normal)
    button.addTarget(self, action: #selector(buttonClicked(sender:)), for: .touchUpInside)
    return button
  }()

  @objc func buttonClicked(sender: NEOneOnOneImageButton) {
    if sender == beginButton {
      delegate?.homeBegin()
    } else if sender == topicButton {
      delegate?.homeTopic()
    } else if sender == skillButton {
      delegate?.homeSkill()
    } else if sender == replayButton {
      delegate?.homeReplay()
    }
  }
}
