// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public class NEOneOnOnePaddingLabel: UILabel {
  var textInsets: UIEdgeInsets = .zero
  override public func drawText(in rect: CGRect) {
    super.drawText(in: rect.inset(by: textInsets))
  }

  override public func textRect(forBounds bounds: CGRect, limitedToNumberOfLines numberOfLines: Int) -> CGRect {
    let insets = textInsets
    var rect = super.textRect(forBounds: bounds.inset(by: insets), limitedToNumberOfLines: numberOfLines)

    rect.origin.x -= insets.left
    rect.origin.y -= insets.top
    rect.size.width += (insets.left + insets.right)
    rect.size.height += (insets.top + insets.bottom)
    return rect
  }
}

/// 文案提示
public class NEOneOnOneCustomMsgSalutation: UIView {
  override public init(frame: CGRect) {
    super.init(frame: frame)
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubViews() {
//        addSubview(backView)
//        backView.snp.makeConstraints { make in
//            make.center.equalTo(self)
//            make.height.equalTo(28)
//            make.width.equalTo(168)
//        }
    addSubview(titleLabel)
    titleLabel.snp.makeConstraints { make in
      make.center.equalTo(self)
      make.height.greaterThanOrEqualTo(28)
      make.width.lessThanOrEqualTo(self).offset(-20)
    }
  }

  // MARK: lazyLoad

//    lazy var backView:UIView = {
//       let backView = UIView()
//        backView.backgroundColor = UIColor.init(hexString: "#342D42",0.6)
//        backView.layer.masksToBounds = true
//        backView.layer.cornerRadius = 12
//        return backView
//    }()
  lazy var titleLabel: NEOneOnOnePaddingLabel = {
    let titleLabel = NEOneOnOnePaddingLabel()
    titleLabel.textColor = UIColor(hexString: "#FFFFFF")
    titleLabel.text = ne_localized("缘分妙不可言，快来聊聊吧")
    titleLabel.font = UIFont(name: "PingFangSC-Regular", size: 12)
    titleLabel.numberOfLines = 0
    titleLabel.backgroundColor = UIColor(hexString: "#342D42", 0.6)
    titleLabel.layer.masksToBounds = true
    titleLabel.layer.cornerRadius = 12
    titleLabel.textInsets = UIEdgeInsets(top: 8, left: 12, bottom: 8, right: 12)
    titleLabel.textAlignment = .center
    return titleLabel
  }()
}

/// 音视频提示
public class NEOneOnOneCustomMsgConnectionSalution: UIView {
  public var connection: ((Bool) -> Void)?

  override public init(frame: CGRect) {
    super.init(frame: frame)
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubViews() {
    addSubview(backView)
    backView.snp.makeConstraints { make in
      make.center.equalTo(self)
      make.height.equalTo(44)
      make.width.equalTo(278)
    }
    backView.addSubview(titleLabel)
    titleLabel.snp.makeConstraints { make in
      make.centerY.equalTo(backView)
      make.left.equalTo(12)
    }
    backView.addSubview(callButton)
    callButton.snp.makeConstraints { make in
      make.centerY.equalTo(backView)
      make.right.equalTo(-12)
      make.height.equalTo(28)
      make.width.equalTo(86)
    }
  }

  public var isAuidoSalution: Bool = false {
    didSet {
      reloadSubViews()
    }
  }

  // MARK: lazyLoad

  lazy var backView: UIView = {
    let backView = UIView()
    backView.backgroundColor = UIColor(hexString: "#342D42", 0.6)
    backView.layer.masksToBounds = true
    backView.layer.cornerRadius = 12
    return backView
  }()

  lazy var titleLabel: UILabel = {
    let titleLabel = UILabel()
    titleLabel.textColor = UIColor(hexString: "#FFFFFF")
    titleLabel.text = ne_localized("试试语音通话，体检TA的声音")
    titleLabel.font = UIFont(name: "PingFangSC-Regular", size: 12)
    titleLabel.contentMode = .center
    return titleLabel
  }()

  lazy var gradientLayer: CAGradientLayer = {
    let gradientColors: [CGColor] = [UIColor(hexString: "#F9627C").cgColor, UIColor(hexString: "#FF8073").cgColor]
    let gradientLayer = CAGradientLayer()
    gradientLayer.frame = CGRectMake(0, 0, 86, 28)
    gradientLayer.colors = gradientColors
    gradientLayer.locations = [0, 1.0]
    return gradientLayer
  }()

  lazy var callButton: UIButton = {
    let callButton = UIButton()
    callButton.setTitle("立即拨打", for: .normal)
    callButton.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
    callButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 12)
    callButton.setImage(ne_chatUI_imageName(imageName: "audio_call_icon"), for: .normal)
    callButton.layer.masksToBounds = true
    callButton.layer.cornerRadius = 14
    callButton.titleEdgeInsets = UIEdgeInsets(top: 0, left: 2.5, bottom: 0, right: 0)
    callButton.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 2.5)
    callButton.layer.addSublayer(gradientLayer)
    callButton.addTarget(self, action: #selector(clickCallButton), for: .touchUpInside)
    if let imageView = callButton.imageView {
      callButton.bringSubviewToFront(imageView)
    }
    if let titleView = callButton.titleLabel {
      callButton.bringSubviewToFront(titleView)
    }
    return callButton
  }()

  @objc func clickCallButton() {
    if let connection = connection {
      connection(isAuidoSalution)
    }
  }

  func reloadSubViews() {
    let title = isAuidoSalution ? "试试语音通话，体检TA的声音" : "试试视频通话，一睹TA的美貌"
    let image = isAuidoSalution ? ne_chatUI_imageName(imageName: "audio_call_icon") : ne_chatUI_imageName(imageName: "video_call_icon")
    titleLabel.text = ne_localized(title)
    callButton.setImage(image, for: .normal)
  }
}

public class NEOneOnOneCustomMsgRewardSalution: UIView {
  public private(set) var isSender: Bool = false
  public var reward: (() -> Void)?
  public var longPress: ((UILongPressGestureRecognizer) -> Void)?

  public init(frame: CGRect, isSender: Bool) {
    super.init(frame: frame)
    self.isSender = isSender
    loadSubViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func setModel(_ model: CustomAttachment) {
    switch model.giftId {
    case 1:
      giftImgae.image = ne_chatUI_imageName(imageName: "gift01_ico")
      countLabel.text = "\(ne_localized("荧光棒")) x\(model.giftCount)"
    case 2:
      giftImgae.image = ne_chatUI_imageName(imageName: "gift02_ico")
      countLabel.text = "\(ne_localized("安排")) x\(model.giftCount)"
    case 3:
      giftImgae.image = ne_chatUI_imageName(imageName: "gift03_ico")
      countLabel.text = "\(ne_localized("跑车")) x\(model.giftCount)"
    case 4:
      giftImgae.image = ne_chatUI_imageName(imageName: "gift04_ico")
      countLabel.text = "\(ne_localized("火箭")) x\(model.giftCount)"
    default: break
    }
  }

  func loadSubViews() {
    addSubview(backgroundImage)
    addSubview(giftImgae)
    addSubview(sendLabel)
    addSubview(countLabel)
    addSubview(sendButton)

    backgroundImage.snp.makeConstraints { make in
      make.left.top.right.bottom.equalToSuperview()
    }
    giftImgae.snp.makeConstraints { make in
      if isSender {
        make.left.equalToSuperview().offset(13)
      } else {
        make.right.equalToSuperview().offset(-13)
      }
      make.width.height.equalTo(88)
      make.centerY.equalToSuperview()
    }
    sendLabel.snp.makeConstraints { make in
      if isSender {
        make.left.equalTo(giftImgae.snp.right)
        make.right.equalToSuperview().offset(-16)
      } else {
        make.right.equalTo(giftImgae.snp.left)
        make.left.equalToSuperview().offset(16)
      }
      make.top.equalToSuperview().offset(12)
      make.height.equalTo(20)
    }
    countLabel.snp.makeConstraints { make in
      make.left.right.equalTo(sendLabel)
      make.top.equalTo(sendLabel.snp.bottom).offset(2)
      make.height.equalTo(20)
    }
    sendButton.snp.makeConstraints { make in
      make.height.equalTo(26)
      if isSender {
        make.right.equalToSuperview().offset(-16)
      } else {
        make.left.equalToSuperview().offset(16)
      }
      make.width.equalTo(70)
      make.top.equalTo(countLabel.snp.bottom).offset(8)
    }
  }

  lazy var backgroundImage: UIImageView = {
    let image = UIImageView(image: ne_chatUI_imageName(imageName: isSender ? "msg_reward_right" : "msg_reward_left"))
    image.isUserInteractionEnabled = true
    let gesture = UILongPressGestureRecognizer(target: self, action: #selector(longPress(gesture:)))
    image.addGestureRecognizer(gesture)
    return image
  }()

  lazy var giftImgae: UIImageView = {
    let image = UIImageView(image: ne_chatUI_imageName(imageName: "gift01_ico"))
    image.isUserInteractionEnabled = true
    let gesture = UILongPressGestureRecognizer(target: self, action: #selector(longPress(gesture:)))
    image.addGestureRecognizer(gesture)
    return image
  }()

  lazy var sendLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(hexString: "#333333")
    label.text = isSender ? ne_localized("送给对方") : ne_localized("送给你礼物")
    label.font = UIFont(name: "PingFangSC-Regular", size: 12)
    label.textAlignment = isSender ? .right : .left
    label.isUserInteractionEnabled = true
    let gesture = UILongPressGestureRecognizer(target: self, action: #selector(longPress(gesture:)))
    label.addGestureRecognizer(gesture)
    return label
  }()

  lazy var countLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(hexString: "#FC435B")
    label.text = "火箭 ×66"
    label.font = UIFont(name: "PingFangSC-Medium", size: 15)
    label.textAlignment = isSender ? .right : .left
    label.isUserInteractionEnabled = true
    label.adjustsFontSizeToFitWidth = true
    let gesture = UILongPressGestureRecognizer(target: self, action: #selector(longPress(gesture:)))
    label.addGestureRecognizer(gesture)
    return label
  }()

  lazy var sendButton: UIButton = {
    let btn = UIButton()
    btn.setTitle(isSender ? ne_localized("继续送礼") : ne_localized("回赠礼物"), for: .normal)
    btn.setBackgroundImage(ne_chatUI_imageName(imageName: "alert_btn"), for: .normal)
    btn.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 12)
    btn.addTarget(self, action: #selector(sendGift), for: .touchUpInside)
    return btn
  }()

  @objc func sendGift() {
    reward?()
  }

  @objc func longPress(gesture: UILongPressGestureRecognizer) {
    longPress?(gesture)
  }
}

var officialAttributeMap = [String: NSMutableAttributedString]()

public class NEOneOnOneOfficialSalution: UIView {
  public var longPress: ((UILongPressGestureRecognizer) -> Void)?

  var thanksForGiven: ((String) -> Void)?
  static func getSize(content: String) -> CGSize {
    let maxWidth: CGFloat = UIScreen.main.bounds.width / 2
    let textContainer = NSTextContainer(size: CGSize(width: maxWidth, height: CGFloat.greatestFiniteMagnitude))
    let layoutManager = NSLayoutManager()
    layoutManager.addTextContainer(textContainer)
    let textStorage = NSTextStorage(attributedString: covertToAttributed(combinedString: content))
    textStorage.addLayoutManager(layoutManager)
    let padding = UIEdgeInsets(top: 8, left: 8, bottom: 8, right: 8) // 内边距
    let rect = layoutManager.usedRect(for: textContainer)
    return CGSize(width: rect.width + padding.left + padding.right + 8, height: rect.height + padding.top + padding.bottom + 8)
  }

  static func covertToAttributed(combinedString: String) -> NSMutableAttributedString {
    if let str = officialAttributeMap[combinedString] {
      return str
    } else {
      if let data = combinedString.data(using: .unicode),
         let attributedString = try? NSMutableAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html], documentAttributes: nil) {
        attributedString.addAttribute(NSAttributedString.Key.font, value: UIFont.systemFont(ofSize: 16), range: NSRange(location: 0, length: attributedString.length))
        officialAttributeMap[combinedString] = attributedString
        return attributedString
      }
      return NSMutableAttributedString(string: combinedString)
    }
  }

  override public init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = .white

    loadSubViews()
    if #available(iOS 11.0, *) {
      layer.cornerRadius = 8
      layer.maskedCorners = [.layerMaxXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMaxYCorner]
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubViews() {
    addSubview(textView)
    textView.snp.makeConstraints { make in
      make.top.left.equalToSuperview().offset(4)
      make.bottom.right.equalToSuperview().offset(-4)
    }
  }

  lazy var textView: UITextView = {
    let view = UITextView(frame: .zero)
    view.backgroundColor = .white
    view.isEditable = false
    view.isScrollEnabled = false
    view.delegate = self
    view.isUserInteractionEnabled = true
    let gesture = UILongPressGestureRecognizer(target: self, action: #selector(longPress(gesture:)))
    view.addGestureRecognizer(gesture)
    return view
  }()

  func setModel(_ model: CustomAttachment) {
    if let msg = model.msg {
      textView.attributedText = NEOneOnOneOfficialSalution.covertToAttributed(combinedString: msg)
    }
  }

  @objc func longPress(gesture: UILongPressGestureRecognizer) {
    longPress?(gesture)
  }
}

extension NEOneOnOneOfficialSalution: UITextViewDelegate {
  @available(iOS 10.0, *)
  public func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange, interaction: UITextItemInteraction) -> Bool {
    // 服务条款
    print(URL.absoluteString)
    // 礼物感谢
    if let thanksForGiven = thanksForGiven {
      thanksForGiven(URL.absoluteString)
    }
    return false
  }
}
