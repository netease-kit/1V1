// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers public class NEOneOnOneBottomConnectView: UIView {
  lazy var chatUpButton: UIButton = {
    let button = UIButton()
    button.setImage(NEOneOnOneUI.ne_imageName("chat_up_icon"), for: .normal)
    button.setImage(NEOneOnOneUI.ne_imageName("chat_up_icon"), for: .highlighted)
    button.setTitle(ne_oneOnOne_localized("搭讪"), for: .normal)
    button.titleLabel?.textColor = UIColor(hexString: "#FFFFFFF")
    button.tintColor = UIColor(hexString: "#FFFFFFF")
    button.backgroundColor = UIColor.clear
    button.layer.masksToBounds = true
    button.layer.cornerRadius = 22
    button.titleLabel?.font = UIFont.systemFont(ofSize: 14)
    let gradientLayer = CAGradientLayer()
    let width = (self.frame.size.width - 23 * 2 - 15) / 2
    gradientLayer.frame = CGRect(x: 0, y: 0, width: width, height: 44)
    gradientLayer.startPoint = CGPoint(x: 0, y: 0)
    gradientLayer.endPoint = CGPoint(x: 1, y: 0)
    gradientLayer.locations = [0.5, 1.0]
    gradientLayer.colors = [
      UIColor(hexString: "#F9627C").cgColor,
      UIColor(hexString: "#FF8073").cgColor,
    ]
    button.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 2)
    button.titleEdgeInsets = UIEdgeInsets(top: 0, left: 2, bottom: 0, right: 0)
    button.layer.insertSublayer(gradientLayer, at: 0)
    button.bringSubviewToFront(button.imageView!)
    button.addTarget(self, action: #selector(clickChatUp(_:)), for: .touchUpInside)
    return button
  }()

  lazy var privateLatterButton: UIButton = {
    let button = UIButton()
    button.setImage(NEOneOnOneUI.ne_imageName("private_letter_icon"), for: .normal)
    button.setImage(NEOneOnOneUI.ne_imageName("private_letter_icon"), for: .highlighted)
    button.addTarget(self, action: #selector(clickPrivateLatterButton(_:)), for: .touchUpInside)
    return button
  }()

  lazy var privateLatterLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("私信")
    label.font = UIFont.systemFont(ofSize: 10)
    label.textColor = UIColor(hexString: "#8E95A9")
    return label
  }()

  lazy var audioButton: UIButton = {
    let button = UIButton()
    button.setImage(NEOneOnOneUI.ne_imageName("audio_call_icon"), for: .normal)
    button.setImage(NEOneOnOneUI.ne_imageName("audio_call_icon"), for: .highlighted)
    button.addTarget(self, action: #selector(clickAudio(_:)), for: .touchUpInside)
    return button
  }()

  lazy var audioLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("语音")
    label.font = UIFont.systemFont(ofSize: 10)
    label.textColor = UIColor(hexString: "#8E95A9")
    return label
  }()

  lazy var videoButton: UIButton = {
    let button = UIButton()
    button.setImage(NEOneOnOneUI.ne_imageName("video_call_icon"), for: .normal)
    button.setImage(NEOneOnOneUI.ne_imageName("video_call_icon"), for: .highlighted)
    button.addTarget(self, action: #selector(clickVideo(_:)), for: .touchUpInside)
    return button
  }()

  lazy var videoLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("视频")
    label.font = UIFont.systemFont(ofSize: 10)
    label.textColor = UIColor(hexString: "#8E95A9")
    return label
  }()

  lazy var bottomBackView: UIView = {
    let view = UIView()
    view.backgroundColor = UIColor.white
    return view
  }()

  var clickAudioAction: (() -> Void)?
  var clickChatUpAction: (() -> Void)?
  var clickPrivateLatterAction: (() -> Void)?
  var clickVideoAction: (() -> Void)?

  override init(frame: CGRect) {
    super.init(frame: frame)
    addSubview(bottomBackView)
    bottomBackView.addSubview(chatUpButton)
    bottomBackView.addSubview(privateLatterButton)
    bottomBackView.addSubview(privateLatterLabel)
    bottomBackView.addSubview(audioButton)
    bottomBackView.addSubview(audioLabel)
    bottomBackView.addSubview(videoButton)
    bottomBackView.addSubview(videoLabel)

    bottomBackView.snp.makeConstraints { make in
      make.top.left.right.bottom.equalToSuperview()
    }
    chatUpButton.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(23)
      make.centerY.equalToSuperview()
      make.width.equalTo((frame.size.width - 23 * 2 - 15) / 2)
      make.height.equalTo(44)
    }
    privateLatterButton.snp.makeConstraints { make in
      make.height.width.equalTo(32)
      make.centerY.equalTo(bottomBackView).offset(-8)
      make.left.equalTo(self.snp.centerX).offset(13)
    }

    privateLatterLabel.snp.makeConstraints { make in
      make.top.equalTo(privateLatterButton.snp.bottom)
      make.centerX.equalTo(privateLatterButton.snp.centerX)
    }

    audioButton.snp.makeConstraints { make in
      make.height.width.equalTo(32)
      make.centerY.equalTo(privateLatterButton.snp.centerY)
      make.left.equalTo(privateLatterButton.snp.right).offset(20)
    }

    audioLabel.snp.makeConstraints { make in
      make.top.equalTo(audioButton.snp.bottom)
      make.centerX.equalTo(audioButton.snp.centerX)
    }

    videoButton.snp.makeConstraints { make in
      make.height.width.equalTo(32)
      make.centerY.equalTo(privateLatterButton.snp.centerY)
      make.left.equalTo(audioButton.snp.right).offset(20)
    }

    videoLabel.snp.makeConstraints { make in
      make.top.equalTo(videoButton.snp.bottom)
      make.centerX.equalTo(videoButton.snp.centerX)
    }

    bottomBackView.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(self)
      make.height.equalTo(94)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func clickAudio(_ sender: UIButton) {
    NotificationCenter.default.post(name: Notification.Name("oneOnOneAudioCall"), object: nil)
    if let clickAudioAction = clickAudioAction {
      clickAudioAction()
    }
  }

  func clickChatUp(_ sender: UIButton) {
    NotificationCenter.default.post(name: Notification.Name("OneOnOneChatUp"), object: nil)
    if let clickChatUpAction = clickChatUpAction {
      clickChatUpAction()
    }
  }

  func clickPrivateLatterButton(_ sender: UIButton) {
    NotificationCenter.default.post(name: Notification.Name("OneOnOnePrivateLetter"), object: nil)
    if let clickPrivateLatterAction = clickPrivateLatterAction {
      clickPrivateLatterAction()
    }
  }

  func clickVideo(_ sender: UIButton) {
    NotificationCenter.default.post(name: Notification.Name("oneOnOneVideoCall"), object: nil)
    if let clickVideoAction = clickVideoAction {
      clickVideoAction()
    }
  }
}
