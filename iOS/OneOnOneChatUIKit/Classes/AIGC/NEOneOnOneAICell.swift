// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit

enum NEOneOnOneAICellAccessoryType {
  case copy
  case detail
}

protocol NEOneOnOneAICellDelegaet: AnyObject {
  func accessoryButtonCopy(content: String?)
  func accessoryButtonDetail(content: String?)
}

class NEOneOnOneAICell: UITableViewCell {
  weak var delegate: NEOneOnOneAICellDelegaet?

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)

    contentView.backgroundColor = UIColor(hexString: "eef5fe")
    contentView.addSubview(contentLabel)
    contentView.addSubview(accessoryButton)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private var _type = NEOneOnOneAICellAccessoryType.copy
  var type: NEOneOnOneAICellAccessoryType {
    set {
      _type = newValue
      switch _type {
      case .copy:
        accessoryButton.setTitle(ne_localized("发送"), for: .normal)
        accessoryButton.backgroundColor = UIColor(hexString: "#F9657B")
      case .detail:
        accessoryButton.setTitle(ne_localized("查看"), for: .normal)
        accessoryButton.backgroundColor = UIColor(hexString: "#5C98FF")
      }
    }
    get {
      _type
    }
  }

  private var _content: String?
  var content: String? {
    set {
      _content = newValue
      updateSubviews()
    }
    get {
      _content
    }
  }

  func updateSubviews() {
    if let content = content {
      contentLabel.text = content
      let maxWidth = UIScreen.main.bounds.width - 26 * 3 - 50
      contentLabel.frame = CGRect(x: 26, y: 14, width: maxWidth, height: NEOneOnOneAICell.calculateLabelHeight(content))

      let accessoryX = UIScreen.main.bounds.width - 50 - 26
      accessoryButton.frame = CGRect(x: Int(accessoryX), y: 0, width: 50, height: 24)
      accessoryButton.centerY = contentLabel.centerY
    }
  }

  static func calculateLabelHeight(_ content: String) -> CGFloat {
    let maxWidth = UIScreen.main.bounds.width - 26 * 3 - 50
    let contentSize = content.boundingRect(with: CGSize(width: maxWidth, height: CGFloat.greatestFiniteMagnitude), options: [.usesLineFragmentOrigin, .usesFontLeading], attributes: [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14)], context: nil)
    return contentSize.height
  }

  static func calculateCellHeight(_ content: String) -> CGFloat {
    calculateLabelHeight(content) + 28
  }

  lazy var contentLabel: UILabel = {
    let view = UILabel(frame: .zero)
    view.textColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1)
    view.font = UIFont.systemFont(ofSize: 14)
    view.numberOfLines = 0
    view.lineBreakMode = .byWordWrapping
    return view
  }()

  lazy var accessoryButton: UIButton = {
    let btn = UIButton(frame: .zero)
    btn.backgroundColor = UIColor(hexString: "#F9657B")
    btn.setTitle(ne_localized("发送"), for: .normal)
    btn.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 12)
    btn.addTarget(self, action: #selector(clicked), for: .touchUpInside)
    btn.layer.cornerRadius = 12
    btn.clipsToBounds = true
    return btn
  }()

  @objc func clicked() {
    switch type {
    case .copy:
      delegate?.accessoryButtonCopy(content: content)
    case .detail:
      delegate?.accessoryButtonDetail(content: content)
    }
  }
}
