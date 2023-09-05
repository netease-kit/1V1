// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NEChatUIKit
import NIMSDK
import UIKit

/// 缘分妙不可言，快来聊聊吧
class NEOneOnOneTextSalutuionCell: NEChatBaseCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var salutionCell = {
    let salutionCell = NEOneOnOneCustomMsgSalutation()
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.center.equalTo(contentView)
      make.width.height.equalTo(contentView)
    }
    selectionStyle = .none
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is custom message")
  }
}

// "⚠️ 注意保护个人隐私安全 ⚠️\n请勿提供第三方联系方式"
class NEOneOnOneTextThirdPrivacyCell: NEChatBaseCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var salutionCell = {
    let salutionCell = NEOneOnOneCustomMsgSalutation()
    salutionCell.titleLabel.text = ne_localized("⚠️ 注意保护个人隐私安全 ⚠️\n请勿提供第三方联系方式")
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.center.equalTo(contentView)
      make.width.height.equalTo(contentView)
    }
    selectionStyle = .none
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is custom message")
  }
}

// "⚠️ 消息内容涉嫌违规 ⚠️\n请注意内容的合法合规"
class NEOneOnOneTextNonComplianceCell: NEChatBaseCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var salutionCell = {
    let salutionCell = NEOneOnOneCustomMsgSalutation()
    salutionCell.titleLabel.text = ne_localized("⚠️ 消息内容涉嫌违规 ⚠️\n请注意内容的合法合规")
    salutionCell.titleLabel.accessibilityIdentifier = "id.tv"
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.center.equalTo(contentView)
      make.width.height.equalTo(contentView)
    }
    selectionStyle = .none
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is custom message")
  }
}

/// 试试语音通话，体检TA的声音
class NEOneOnOneAudioSalutuionCell: NEChatBaseCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var salutionCell: NEOneOnOneCustomMsgConnectionSalution = {
    let salutionCell = NEOneOnOneCustomMsgConnectionSalution()
    salutionCell.isAuidoSalution = true
    salutionCell.connection = { isAudioSalution in
      if isAudioSalution {
        // 音频
        NotificationCenter.default.post(name: NSNotification.Name(AudioCallAction), object: nil)
      }
    }
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.center.equalTo(contentView)
      make.width.height.equalTo(contentView)
    }
    selectionStyle = .none
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is audio custom message")
  }
}

/// 试试视频通话，一睹TA的美貌
class NEOneOnOneVideoSalutuionCell: NEChatBaseCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var salutionCell: NEOneOnOneCustomMsgConnectionSalution = {
    let salutionCell = NEOneOnOneCustomMsgConnectionSalution()
    salutionCell.isAuidoSalution = false

    salutionCell.connection = { isAudioSalution in
      if !isAudioSalution {
        // 视频
        NotificationCenter.default.post(name: NSNotification.Name(VideoCallAction), object: nil)
      }
    }
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.center.equalTo(contentView)
      make.width.height.equalTo(contentView)
    }
    selectionStyle = .none
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is video custom message")
  }
}

// 礼物消息
class NEOneOnOneRewardRightCell: NEBaseChatMessageCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  override func showLeftOrRight(showRight: Bool) {
    super.showLeftOrRight(showRight: showRight)
    bubbleImageRight.isHidden = true
  }

  lazy var salutionCell: NEOneOnOneCustomMsgRewardSalution = {
    let salutionCell = NEOneOnOneCustomMsgRewardSalution(frame: .zero, isSender: true)
    // IMUIKit没法把delegate设置到ViewController去，只能先用NotificationCenter来通知
    salutionCell.reward = {
      NotificationCenter.default.post(name: NSNotification.Name("SendGift"), object: nil)
    }
    salutionCell.longPress = { [weak self] gesture in
      self?.longPress(longPress: gesture)
    }
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.bottom.equalToSuperview()
      make.top.equalToSuperview().offset(12)
      make.width.equalTo(220)
      make.right.equalTo(avatarImageRight.snp.left).offset(-8)
    }
    selectionStyle = .none
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is reward message")
    super.setModel(model)
    if let messageObject = model.message?.messageObject as? NIMCustomObject,
       let attach = messageObject.attachment as? CustomAttachment {
      salutionCell.setModel(attach)
    }
  }
}

// 礼物消息
class NEOneOnOneRewardLeftCell: NEBaseChatMessageCell {
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
  }

  override func showLeftOrRight(showRight: Bool) {
    super.showLeftOrRight(showRight: showRight)
    bubbleImageLeft.isHidden = true
  }

  lazy var salutionCell: NEOneOnOneCustomMsgRewardSalution = {
    let salutionCell = NEOneOnOneCustomMsgRewardSalution(frame: .zero, isSender: false)
    // IMUIKit没法把delegate设置到ViewController去，只能先用NotificationCenter来通知
    salutionCell.reward = {
      NotificationCenter.default.post(name: NSNotification.Name("SendGift"), object: nil)
    }
    salutionCell.longPress = { [weak self] gesture in
      self?.longPress(longPress: gesture)
    }
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.bottom.equalToSuperview()
      make.top.equalToSuperview().offset(12)
      make.width.equalTo(220)
      make.left.equalTo(avatarImageLeft.snp.right).offset(8)
    }
    selectionStyle = .none
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is reward message")
    super.setModel(model)
    if let messageObject = model.message?.messageObject as? NIMCustomObject,
       let attach = messageObject.attachment as? CustomAttachment {
      salutionCell.setModel(attach)
    }
  }
}

// 小秘书礼物消息
class NEOneOnOneOfficialCell: NEBaseChatMessageCell {
  public var thanksForGiven: ((String) -> Void)?
  override func awakeFromNib() {
    super.awakeFromNib()
    // Initialization code
    accessibilityIdentifier = "id.contentWithAllLayer"
  }

  override func showLeftOrRight(showRight: Bool) {
    super.showLeftOrRight(showRight: showRight)
    bubbleImageLeft.isHidden = true
  }

  lazy var salutionCell: NEOneOnOneOfficialSalution = {
    let salutionCell = NEOneOnOneOfficialSalution(frame: .zero)
    salutionCell.thanksForGiven = { data in
      if let thanksForGiven = self.thanksForGiven {
        thanksForGiven(data)
      }
    }
    salutionCell.longPress = { [weak self] gesture in
      self?.longPress(longPress: gesture)
    }
    return salutionCell
  }()

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.addSubview(salutionCell)
    salutionCell.snp.makeConstraints { make in
      make.bottom.equalToSuperview()
      make.width.equalTo(200)
      make.height.equalTo(200)
      make.left.equalTo(avatarImageLeft.snp.right).offset(8)
    }
    selectionStyle = .none
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func setModel(_ model: MessageContentModel) {
    print("this is official message")
    super.setModel(model)
    if let messageObject = model.message?.messageObject as? NIMCustomObject,
       let attach = messageObject.attachment as? CustomAttachment {
      salutionCell.setModel(attach)
      if let msg = attach.msg {
        let size = NEOneOnOneOfficialSalution.getSize(content: msg)
        salutionCell.snp.updateConstraints { make in
          make.bottom.equalToSuperview()
          make.width.equalTo(size.width)
          make.height.equalTo(size.height)
          make.left.equalTo(avatarImageLeft.snp.right).offset(8)
        }
      }
    }
  }
}
