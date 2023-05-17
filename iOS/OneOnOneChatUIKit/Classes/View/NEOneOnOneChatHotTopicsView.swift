// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECommonUIKit

@objcMembers
public class NEOneOnOneChatHotTopicsView: UIView {
  public typealias CompletionCallback = (Int) -> Void
  var selectTopicCallback: CompletionCallback?
  /// Y position 用于做动画
  var showYPositon = 0.0

  /// 内部数据源
  var _topicArray: [String] = .init()

  var topicArray: [String] {
    get {
      _topicArray
    }
    set {
      _topicArray = newValue
      hotTopicTableView.reloadData()
    }
  }

  override public func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    dismissView(nil)
  }

  override public init(frame: CGRect) {
    super.init(frame: CGRect(x: frame.origin.x, y: frame.size.height, width: frame.size.width, height: frame.size.height))
    showYPositon = frame.origin.y
    setUpSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func setUpSubViews() {
    addSubview(backView)
    NSLayoutConstraint.activate([
      backView.leftAnchor.constraint(equalTo: leftAnchor),
      backView.rightAnchor.constraint(equalTo: rightAnchor),
      backView.bottomAnchor.constraint(equalTo: bottomAnchor),
      backView.heightAnchor.constraint(equalToConstant: 444),
    ])

    backView.addSubview(titleLabel)
    NSLayoutConstraint.activate([
      titleLabel.topAnchor.constraint(equalTo: backView.topAnchor, constant: 20),
      titleLabel.leftAnchor.constraint(equalTo: leftAnchor),
      titleLabel.rightAnchor.constraint(equalTo: rightAnchor),
      titleLabel.heightAnchor.constraint(equalToConstant: 24),

    ])

    addSubview(hotTopicTableView)
    NSLayoutConstraint.activate([
      hotTopicTableView.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 16),
      hotTopicTableView.leftAnchor.constraint(equalTo: leftAnchor),
      hotTopicTableView.rightAnchor.constraint(equalTo: rightAnchor),
      hotTopicTableView.bottomAnchor.constraint(equalTo: bottomAnchor),
//            hotTopicTableView.heightAnchor.constraint(equalToConstant: 390)
    ])
  }

  // MARK: lazyLoad

  lazy var titleLabel: UILabel = {
    let titleLabel = UILabel()
    titleLabel.translatesAutoresizingMaskIntoConstraints = false
    titleLabel.text = ne_localized("热聊话题")
    titleLabel.font = UIFont(name: "PingFangSC-Medium", size: 16)
    titleLabel.textAlignment = .center
    titleLabel.textColor = UIColor.black
    return titleLabel
  }()

  lazy var hotTopicTableView: UITableView = {
    let hotTopicTableView = UITableView()
    hotTopicTableView.translatesAutoresizingMaskIntoConstraints = false
    hotTopicTableView.delegate = self
    hotTopicTableView.dataSource = self
    hotTopicTableView.register(NEOneOnOneChatHotTopicsCell.self, forCellReuseIdentifier: "hotTopicTableViewIdentifier")
    hotTopicTableView.backgroundColor = .clear
    hotTopicTableView.separatorStyle = .none
    return hotTopicTableView
  }()

  lazy var backView: UIView = {
    let backView = UIView(frame: CGRectMake(0, 0, UIScreen.main.bounds.self.width, UIScreen.main.bounds.self.height))
    backView.translatesAutoresizingMaskIntoConstraints = false
    setCornersRadius(backView, radius: 5.0, roundingCorners: [[.topLeft, .topRight]])
    backView.backgroundColor = UIColor.white
    backView.layer.masksToBounds = true

    return backView
  }()

  // MARK: public func

  public func showView(callback: (() -> Void)?) {
    DispatchQueue.main.async {
      self.endEditing(true)
      self.frame = CGRectMake(self.frame.origin.x, self.showYPositon + self.frame.size.height, self.frame.size.width, self.frame.size.height)
      UIView.animate(withDuration: 0.3) {
        self.frame = CGRectMake(self.frame.origin.x, self.showYPositon, self.frame.size.width, self.frame.size.height)
      } completion: { finish in
        callback?()
      }
    }
  }

  public func dismissView(_ callback: (() -> Void)?) {
    DispatchQueue.main.async {
      UIView.animate(withDuration: 0.3) {
        self.frame = CGRectMake(self.frame.origin.x, self.showYPositon + self.frame.size.height, self.frame.size.width, self.frame.size.height)
      } completion: { finish in
        callback?()
      }
    }
  }

  func setCornersRadius(_ view: UIView!, radius: CGFloat, roundingCorners: UIRectCorner) {
    if view == nil {
      return
    }
    let maskPath = UIBezierPath(roundedRect: view.bounds, byRoundingCorners: roundingCorners, cornerRadii: CGSize(width: radius, height: radius))
    let maskLayer = CAShapeLayer()
    maskLayer.frame = view.bounds
    maskLayer.path = maskPath.cgPath
    maskLayer.shouldRasterize = true
    maskLayer.rasterizationScale = UIScreen.main.scale

    view.layer.mask = maskLayer
  }
}

extension NEOneOnOneChatHotTopicsView: UITableViewDelegate, UITableViewDataSource {
  public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    topicArray.count
  }

  public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if let cell = tableView.dequeueReusableCell(withIdentifier: "hotTopicTableViewIdentifier", for: indexPath) as? NEOneOnOneChatHotTopicsCell {
      cell.contentLabel.text = _topicArray[indexPath.row]
      return cell
    } else {
      return UITableViewCell()
    }
  }

  public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    56
  }

  public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    if _topicArray.count > indexPath.row {
      if let selectTopicCallback = selectTopicCallback {
        selectTopicCallback(indexPath.row)
        dismissView(nil)
      }
    }
  }
}

class NEOneOnOneChatHotTopicsCell: UITableViewCell {
  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    backgroundColor = .clear
    selectionStyle = .none
    contentView.backgroundColor = .clear
    setUpSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func setUpSubViews() {
    contentView.addSubview(pinkBackView)
    NSLayoutConstraint.activate([
      pinkBackView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor, constant: 0),
      pinkBackView.leftAnchor.constraint(equalTo: contentView.leftAnchor, constant: 16),
      pinkBackView.rightAnchor.constraint(equalTo: contentView.rightAnchor, constant: -16),
      pinkBackView.heightAnchor.constraint(equalToConstant: 44),
    ]
    )

    pinkBackView.addSubview(contentLabel)
    NSLayoutConstraint.activate([
      contentLabel.leftAnchor.constraint(equalTo: pinkBackView.leftAnchor, constant: 16),
      contentLabel.centerYAnchor.constraint(equalTo: pinkBackView.centerYAnchor, constant: 0),
      contentLabel.rightAnchor.constraint(equalTo: pinkBackView.rightAnchor, constant: 0),
      contentLabel.heightAnchor.constraint(equalToConstant: 44),
    ]
    )
  }

  lazy var pinkBackView: UIView = {
    let pinkBackView = UIView()
    pinkBackView.translatesAutoresizingMaskIntoConstraints = false
    pinkBackView.backgroundColor = UIColor(hexString: "#FFCDD5")
    pinkBackView.layer.masksToBounds = true
    pinkBackView.layer.cornerRadius = 5
    return pinkBackView
  }()

  lazy var contentLabel = {
    let label = UILabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.font = UIFont.systemFont(ofSize: 14)
    label.textColor = UIColor(hexString: "#333333")
    return label
  }()
}
