// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import NIMSDK
import NEConversationKit
import NECommonKit
import NEConversationUIKit

open class NEOneOnOneConversionCell: UITableViewCell {
  public var topStickInfos = [NIMSession: NIMStickTopSessionInfo]()
  private let repo = ConversationRepo()
  private var timeWidth: NSLayoutConstraint?

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    setupSubviews()
    initSubviewsLayout()
  }

  public required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func setupSubviews() {
    selectionStyle = .none
    contentView.addSubview(headImge)
    contentView.addSubview(redAngleView)
    contentView.addSubview(title)
    contentView.addSubview(subTitle)
    contentView.addSubview(timeLabel)
    contentView.addSubview(notifyMsg)
    contentView.addSubview(onlineImage)
    contentView.addSubview(officialLabel)

    NSLayoutConstraint.activate([
      headImge.leftAnchor.constraint(
        equalTo: contentView.leftAnchor,
        constant: NEConstant.screenInterval
      ),
      headImge.centerYAnchor.constraint(equalTo: contentView.centerYAnchor),
      headImge.widthAnchor.constraint(equalToConstant: 56),
      headImge.heightAnchor.constraint(equalToConstant: 56),
    ])

    NSLayoutConstraint.activate([
      redAngleView.centerXAnchor.constraint(equalTo: headImge.rightAnchor, constant: -8),
      redAngleView.centerYAnchor.constraint(equalTo: headImge.topAnchor, constant: 8),
      redAngleView.heightAnchor.constraint(equalToConstant: 18),
    ])
    timeWidth = timeLabel.widthAnchor.constraint(equalToConstant: 0)
    timeWidth?.isActive = true
    NSLayoutConstraint.activate([
      timeLabel.rightAnchor.constraint(
        equalTo: contentView.rightAnchor,
        constant: -NEConstant.screenInterval
      ),
      timeLabel.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 17),
    ])

    NSLayoutConstraint.activate([
      title.leftAnchor.constraint(equalTo: headImge.rightAnchor, constant: 12),
      title.centerYAnchor.constraint(equalTo: timeLabel.centerYAnchor),
    ])
    titleWidth = title.widthAnchor.constraint(equalToConstant: width)
    titleWidth?.isActive = true

    NSLayoutConstraint.activate([
      officialLabel.leftAnchor.constraint(equalTo: title.rightAnchor, constant: 5),
      officialLabel.centerYAnchor.constraint(equalTo: title.centerYAnchor),
      officialLabel.widthAnchor.constraint(equalToConstant: 32),
      officialLabel.heightAnchor.constraint(equalToConstant: 16),
    ])

    NSLayoutConstraint.activate([
      subTitle.leftAnchor.constraint(equalTo: headImge.rightAnchor, constant: 12),
      subTitle.rightAnchor.constraint(equalTo: contentView.rightAnchor, constant: -50),
      subTitle.topAnchor.constraint(equalTo: title.bottomAnchor, constant: 6),
    ])

    NSLayoutConstraint.activate([
      notifyMsg.rightAnchor.constraint(equalTo: timeLabel.rightAnchor),
      notifyMsg.topAnchor.constraint(equalTo: timeLabel.bottomAnchor, constant: 5),
      notifyMsg.widthAnchor.constraint(equalToConstant: 13),
      notifyMsg.heightAnchor.constraint(equalToConstant: 13),
    ])

    NSLayoutConstraint.activate([
      onlineImage.rightAnchor.constraint(equalTo: headImge.rightAnchor, constant: -2),
      onlineImage.bottomAnchor.constraint(equalTo: headImge.bottomAnchor, constant: -2),
      onlineImage.widthAnchor.constraint(equalToConstant: 10),
      onlineImage.heightAnchor.constraint(equalToConstant: 10),
    ])

    let line = UIView(frame: .zero)
    line.translatesAutoresizingMaskIntoConstraints = false
    line.backgroundColor = UIColor(red: 234 / 255, green: 234 / 255, blue: 234 / 255, alpha: 1)
    contentView.addSubview(line)
    NSLayoutConstraint.activate([
      line.leftAnchor.constraint(equalTo: headImge.rightAnchor, constant: 12),
      line.rightAnchor.constraint(equalTo: contentView.rightAnchor),
      line.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -1),
      line.heightAnchor.constraint(equalToConstant: 1),
    ])
  }

  var titleWidth: NSLayoutConstraint?

  func updateTitleLayout() {
    let attributes = [NSAttributedString.Key.font: title.font as Any]
    let attributedString = NSAttributedString(string: title.text ?? "", attributes: attributes)
    let size = attributedString.boundingRect(with: CGSize(width: CGFloat.greatestFiniteMagnitude, height: title.frame.height), options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil).size
    let width = ceil(size.width)
    // 大概计算一个最大长度
    let time = timeWidth?.constant ?? 0
    let maxWidth = self.width - 56 - time - 12 - 32 - NEConstant.screenInterval * 2 - 10
    titleWidth?.constant = min(width, maxWidth)
  }

  func initSubviewsLayout() {
    if NEKitConversationConfig.shared.ui.avatarType == .rectangle {
      headImge.layer.cornerRadius = NEKitConversationConfig.shared.ui.avatarCornerRadius
    } else if NEKitConversationConfig.shared.ui.avatarType == .cycle {
      headImge.layer.cornerRadius = 28.0
    }
  }

  func configData(sessionModel: ConversationListModel?) {
    guard let conversationModel = sessionModel else { return }

    if conversationModel.recentSession?.session?.sessionType == .P2P {
      // p2p head image
      if let imageName = conversationModel.userInfo?.userInfo?.avatarUrl {
        headImge.setTitle("")
        headImge.sd_setImage(with: URL(string: imageName), completed: nil)
      } else {
        headImge.setTitle(conversationModel.userInfo?.showName(false) ?? "")
        headImge.sd_setImage(with: nil, completed: nil)
        headImge.backgroundColor = UIColor
          .colorWithString(string: conversationModel.userInfo?.userId)
      }

      // p2p nickName
      title.text = conversationModel.userInfo?.showName()

      // notifyForNewMsg
//      notifyMsg.isHidden = viewModel
//        .notifyForNewMsg(userId: conversationModel.userInfo?.userId)
      notifyMsg.isHidden = repo.isNeedNotify(userId: conversationModel.userInfo?.userId)

    } else if conversationModel.recentSession?.session?.sessionType == .team {
      // team head image
      if let imageName = conversationModel.teamInfo?.avatarUrl {
        headImge.setTitle("")
        headImge.sd_setImage(with: URL(string: imageName), completed: nil)
      } else {
        headImge.setTitle(conversationModel.teamInfo?.getShowName() ?? "")
        headImge.sd_setImage(with: nil, completed: nil)
        headImge.backgroundColor = UIColor
          .colorWithString(string: conversationModel.teamInfo?.teamId)
      }
      title.text = conversationModel.teamInfo?.getShowName()

      // notifyForNewMsg
//      let teamNotifyState = viewModel
//        .notifyStateForNewMsg(teamId: conversationModel.teamInfo?.teamId)
      let teamNotifyState = repo.isNeedNotifyForTeam(teamId: conversationModel.teamInfo?.teamId)
      notifyMsg.isHidden = teamNotifyState == .none ? false : true
    }

    // last message
    if let lastMessage = conversationModel.recentSession?.lastMessage {
      let text = contentForRecentSession(message: lastMessage)
      let mutaAttri = NSMutableAttributedString()
      // readed
      if let readed = conversationModel.recentSession?.lastMessage?.isRemoteRead,
         readed {
        // 已读
        let attachment = NSTextAttachment()
        attachment.image = ne_chatUI_imageName(imageName: "read")
        attachment.bounds = CGRect(x: 0, y: (NEKitConversationConfig.shared.ui.subTitleFont.capHeight - 13) / 2, width: 13, height: 13)
        mutaAttri.append(NSAttributedString(attachment: attachment))
        mutaAttri.append(NSAttributedString(string: " "))
      }
      mutaAttri.append(NSMutableAttributedString(string: text))
      if let sessionId = sessionModel?.recentSession?.session?.sessionId {
        let isAtMessage = NEAtMessageManager.instance.isAtCurrentUser(sessionId: sessionId)
        if isAtMessage == true {
          let atStr = ne_localized("you_were_mentioned")
          mutaAttri.insert(NSAttributedString(string: atStr), at: 0)
          mutaAttri.addAttribute(NSAttributedString.Key.foregroundColor, value: UIColor(hexString: "#E6605C"), range: NSMakeRange(0, atStr.count))
          mutaAttri.addAttribute(NSAttributedString.Key.font, value: NEKitConversationConfig.shared.ui.subTitleFont, range: NSMakeRange(0, mutaAttri.length))
        }
      }
      subTitle.attributedText = mutaAttri // contentForRecentSession(message: lastMessage)
    }

    // unRead message count
    if let unReadCount = conversationModel.recentSession?.unreadCount {
      if unReadCount <= 0 {
        redAngleView.isHidden = true
      } else {
        redAngleView.isHidden = notifyMsg.isHidden ? false : true
        if unReadCount <= 99 {
          redAngleView.text = "\(unReadCount)"
        } else {
          redAngleView.text = "99+"
        }
      }
    }

    // time
    if let rencentSession = conversationModel.recentSession {
      timeLabel
        .text =
        dealTime(time: timestampDescriptionForRecentSession(recentSession: rencentSession))
      if let text = timeLabel.text {
        let maxSize = CGSize(width: UIScreen.main.bounds.width, height: 0)
        let attibutes = [NSAttributedString.Key.font: timeLabel.font]
        let labelSize = NSString(string: text).boundingRect(with: maxSize, attributes: attibutes as [NSAttributedString.Key: Any], context: nil)
        timeWidth?.constant = labelSize.width + 1 // ceil()
      }
    }

    // backgroundColor
    if let session = conversationModel.recentSession?.session {
      let isTop = topStickInfos[session] != nil
      if isTop {
        contentView.backgroundColor = UIColor(hexString: "0xF3F5F7")
      } else {
        contentView.backgroundColor = .white
      }
    }

//    if let ext = conversationModel.localExtension {
//      // online
//      if let online = ext["online"] as? Bool,
//         online {
//        // 在线
//        onlineImage.isHidden = false
//      } else {
//        // 非在线
//        onlineImage.isHidden = true
//      }
//
//      // tag
//      if let official = ext["official"] as? Bool,
//         official {
//        // 官方
//        officialLabel.isHidden = false
//      } else {
//        // 非官方
//        officialLabel.isHidden = true
//      }
//    } else {
//      onlineImage.isHidden = true
//      officialLabel.isHidden = true
//    }
    if conversationModel.userInfo?.userId == "yunxinassistaccid_1" {
      officialLabel.isHidden = false
    } else {
      officialLabel.isHidden = true
    }
    updateTitleLayout()
  }

  func timestampDescriptionForRecentSession(recentSession: NIMRecentSession) -> TimeInterval {
    if let lastMessage = recentSession.lastMessage {
      return lastMessage.timestamp
    }
    // 服务端时间戳以毫秒为单位,需要转化
    return recentSession.updateTime / 1000
  }

  func dealTime(time: TimeInterval) -> String {
    let targetDate = Date(timeIntervalSince1970: time)
    let fmt = DateFormatter()

    if targetDate.isToday() {
      fmt.dateFormat = ne_localized("hm")
      return fmt.string(from: targetDate)

    } else {
      if targetDate.isThisYear() {
        fmt.dateFormat = ne_localized("mdhm")
        return fmt.string(from: targetDate)

      } else {
        fmt.dateFormat = ne_localized("ymdhm")
        return fmt.string(from: targetDate)
      }
    }
  }

  func contentForRecentSession(message: NIMMessage) -> String {
//    let text = NEOneOnOneMessageUtil.messageContent(message: message)
//    return text
    ""
  }

  // MARK: lazy Method

  lazy var headImge: NEUserHeaderView = {
    let headView = NEUserHeaderView(frame: .zero)
    headView.titleLabel.textColor = .white
    headView.titleLabel.font = NEConstant.defaultTextFont(14)
    headView.translatesAutoresizingMaskIntoConstraints = false
    headView.layer.cornerRadius = 28
    headView.clipsToBounds = true
    return headView
  }()

  private lazy var redAngleView: RedAngleLabel = {
    let label = RedAngleLabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.font = NEConstant.defaultTextFont(12)
    label.textColor = .white
    label.text = "99+"
    label.backgroundColor = NEConstant.hexRGB(0xF24957)
    label.textInsets = UIEdgeInsets(top: 3, left: 7, bottom: 3, right: 7)
    label.layer.cornerRadius = 9
    label.clipsToBounds = true
    label.isHidden = true
    return label
  }()

  private lazy var title: UILabel = {
    let label = UILabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.textColor = NEKitConversationConfig.shared.ui.titleColor
    label.font = NEKitConversationConfig.shared.ui.titleFont
    label.text = "Oliver"
    return label
  }()

  private lazy var subTitle: UILabel = {
    let label = UILabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.textColor = NEKitConversationConfig.shared.ui.subTitleColor
    label.font = NEKitConversationConfig.shared.ui.subTitleFont
    return label
  }()

  private lazy var timeLabel: UILabel = {
    let label = UILabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.textColor = NEKitConversationConfig.shared.ui.timeColor
    label.font = NEKitConversationConfig.shared.ui.timeFont
    label.textAlignment = .right
    return label
  }()

  private lazy var notifyMsg: UIImageView = {
    let notify = UIImageView()
    notify.translatesAutoresizingMaskIntoConstraints = false
    notify.image = UIImage.ne_imageNamed(name: "noNeed_notify")
    return notify
  }()

  private lazy var onlineImage: UIImageView = {
    let online = UIImageView()
    online.translatesAutoresizingMaskIntoConstraints = false
    online.image = ne_chatUI_imageName(imageName: "online_point")
    online.isHidden = true
    return online
  }()

  private lazy var officialLabel: UILabel = {
    var view = UILabel(frame: CGRect(x: 0, y: 0, width: 32, height: 16))
    view.translatesAutoresizingMaskIntoConstraints = false
    view.backgroundColor = UIColor(hexString: "#2A6BF2", 0.2)
    view.textColor = UIColor(hexString: "#2A6BF2")
    view.font = UIFont(name: "PingFangSC-Regular", size: 10)
    view.layer.cornerRadius = 8
    view.clipsToBounds = true
    view.text = ne_localized("官方")
    view.textAlignment = .center
    view.isHidden = true
    return view
  }()
}
