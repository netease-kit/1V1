// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public class NEOneOnOneQuickReply: UIView, UICollectionViewDataSource, UICollectionViewDelegate {
  public var oneOnOneQuickReplyArray: [String] = [ne_localized("嗨！我们距离很近"), ne_localized("缘分让我们相遇"), ne_localized("喜欢你的人匆匆赶来了")] {
    didSet {
      quickReplayCollectionView.reloadData()
    }
  }

  public typealias SelectQuickReply = (Int) -> Void
  public var selectQuickReply: SelectQuickReply?
  public var rebortClicked: (() -> Void)?

  // MARK: UICollectionViewDataSource

  public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    oneOnOneQuickReplyArray.count
  }

  public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let cell: NEOneOnOneQuickReplyCell = collectionView.dequeueReusableCell(withReuseIdentifier: NEOneOnOneQuickReplyCell.description(), for: indexPath) as! NEOneOnOneQuickReplyCell
    cell.backgroundColor = UIColor.clear
    cell.titleButton.setTitle(oneOnOneQuickReplyArray[indexPath.row], for: .normal)
    cell.clickComplete = {
      guard let selectQuickReply = self.selectQuickReply else { return }
      selectQuickReply(indexPath.row)
    }
    return cell
  }

  public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    guard let selectQuickReply = selectQuickReply else { return }
    selectQuickReply(indexPath.row)
  }

  // MARK: NEOneOnOneQuickReply

  override public init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = UIColor.clear
    addSubview(rebortButton)
    rebortButton.snp.makeConstraints { make in
      make.right.equalToSuperview().offset(-16)
      make.top.equalToSuperview()
      make.width.height.equalTo(48)
    }
    addSubview(quickReplayCollectionView)
    quickReplayCollectionView.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(self)
      make.height.equalTo(30)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var quickReplayCollectionView: UICollectionView = {
    var quickReplayLayout = UICollectionViewFlowLayout()
    quickReplayLayout.estimatedItemSize = CGSize(width: 60, height: 22)
    quickReplayLayout.scrollDirection = .horizontal // 竖直
    quickReplayLayout.sectionInset = UIEdgeInsets(top: 0, left: 10, bottom: 0, right: 10)
    let quickReplayCollectionView = UICollectionView(frame: .zero, collectionViewLayout: quickReplayLayout)
    quickReplayCollectionView.register(NEOneOnOneQuickReplyCell.self, forCellWithReuseIdentifier: NEOneOnOneQuickReplyCell.description())
    quickReplayCollectionView.dataSource = self
    quickReplayCollectionView.delegate = self
    quickReplayCollectionView.backgroundColor = .clear
    quickReplayCollectionView.showsHorizontalScrollIndicator = false
    return quickReplayCollectionView
  }()

  lazy var rebortButton: UIButton = {
    let button = UIButton()
    button.setImage(ne_chatUI_imageName(imageName: "ai_rebort"), for: .normal)
    button.addTarget(self, action: #selector(clickRebort), for: .touchUpInside)
    button.isHidden = !NEOneOnOneChatRegisterEngine.getInstance().isSupportAIGC
    return button
  }()

  @objc func clickRebort() {
    rebortClicked?()
  }
}

class NEOneOnOneQuickReplyCell: UICollectionViewCell {
  public typealias ClickComplete = () -> Void
  public var clickComplete: ClickComplete?

  override init(frame: CGRect) {
    super.init(frame: frame)
    titleButton.frame = contentView.bounds
    contentView.addSubview(titleButton)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func preferredLayoutAttributesFitting(_ layoutAttributes: UICollectionViewLayoutAttributes) -> UICollectionViewLayoutAttributes {
    let size = titleButton.titleLabel?.text?.size(withAttributes: [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 12)]) ?? CGSize.zero
    let att = super.preferredLayoutAttributesFitting(layoutAttributes)
    att.frame = CGRect(x: 0, y: 0, width: size.width + 28, height: 30)
    titleButton.frame = CGRect(x: 0, y: 0, width: att.frame.size.width, height: 30)
    return att
  }

  lazy var titleButton: UIButton = {
    let titleButton = UIButton()
    titleButton.titleLabel?.textColor = UIColor(hexString: "#FFFFFF")
    titleButton.setTitle(ne_localized("缘分妙不可言，快来聊聊吧"), for: .normal)
    titleButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 12)
    titleButton.backgroundColor = UIColor(hexString: "#342D42", 0.6)
    titleButton.layer.masksToBounds = true
    titleButton.layer.cornerRadius = 15
//        titleButton.titleEdgeInsets = UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 12)
    titleButton.addTarget(self, action: #selector(clickTitleButton), for: .touchUpInside)
    titleButton.tintColor = UIColor(hexString: "#FFFFFF")
    titleButton.accessibilityIdentifier = "id.conversationPrompts"
    return titleButton
  }()

  @objc func clickTitleButton() {
    if let clickComplete = clickComplete {
      clickComplete()
    }
  }
}

class NEOneOnOneSpeakButton: UIButton {
  override init(frame: CGRect) {
    super.init(frame: frame)
    setTitle("按住说话", for: .normal)
    setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
    titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 16)
//    setImage(ne_chatUI_imageName(imageName: "audio_call_icon"), for: .normal)
//    setImage(ne_chatUI_imageName(imageName: "audio_call_icon"), for: .highlighted)

    let gradientColors: [CGColor] = [UIColor(hexString: "#F9627C").cgColor, UIColor(hexString: "#FF8073").cgColor]
    let gradientLayer = CAGradientLayer()
    gradientLayer.frame = CGRectMake(0, 0, 500, 50)
    gradientLayer.colors = gradientColors
    //        gradientLayer.locations = [0, 1.0]
    gradientLayer.startPoint = CGPointMake(0, 0.5)
    gradientLayer.endPoint = CGPointMake(1, 0.5)
    layer.masksToBounds = true
    layer.cornerRadius = 5
    //        self.titleEdgeInsets = UIEdgeInsets(top: 0, left: 2.5, bottom: 0, right: 0)
    //        self.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 2.5)
    layer.addSublayer(gradientLayer)
    //    self.addTarget(self, action: #selector(clickself), for: .touchDown)
    if let imageView = imageView {
      bringSubviewToFront(imageView)
    }
    if let titleView = titleLabel {
      bringSubviewToFront(titleView)
    }
    titleLabel?.accessibilityIdentifier = "id.chatMessageInputAduioTv"
    accessibilityIdentifier = "id.chatMessageInputAduioIv"
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
}
