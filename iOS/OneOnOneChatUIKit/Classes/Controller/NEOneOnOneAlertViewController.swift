// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import UIKit
import SnapKit

public class NEOneOnOneAlertAction: NSObject {
  public var title: String?
  public var handler: (() -> Void)?

  public convenience init(title: String?, handler: (() -> Void)? = nil) {
    self.init()
    self.title = title
    self.handler = handler
  }
}

public class NEOneOnOneAlertViewController: UIViewController {
  public var content: String?
  public var leftAction: NEOneOnOneAlertAction?
  public var rightAction: NEOneOnOneAlertAction?

  convenience init(title: String, content: String, leftAction: NEOneOnOneAlertAction, rightAction: NEOneOnOneAlertAction) {
    self.init()
    self.title = title
    self.content = content
    self.leftAction = leftAction
    self.rightAction = rightAction
    modalPresentationStyle = .overFullScreen
  }

  override public func viewDidLoad() {
    super.viewDidLoad()

    view.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.5)

    view.addSubview(bgView)
    bgView.addSubview(titleLabel)
    bgView.addSubview(textView)
    bgView.addSubview(disagreeButton)
    bgView.addSubview(agreeButton)

    updateViewConstrain(contentHeight: calculateHeight(string: textView.attributedText.string, width: 270, font: UIFont(name: "PingFangSC-Regular", size: 15) ?? UIFont.systemFont(ofSize: 15)))
  }

  func calculateHeight(string: String, width: CGFloat, font: UIFont) -> CGFloat {
    if string.count < 1 {
      return 0
    }

    let height = NSString(string: string).boundingRect(with: CGSize(width: width, height: CGFloat(MAXFLOAT)),
                                                       options: .usesLineFragmentOrigin,
                                                       attributes: [NSAttributedString.Key.font: font],
                                                       context: nil).size.height
    return height + 1
  }

  lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.font = UIFont(name: "PingFangSC-Medium", size: 18)
    label.textColor = UIColor(red: 0.133, green: 0.133, blue: 0.133, alpha: 1)
    label.textAlignment = .center
    label.text = title
    return label
  }()

  lazy var bgView: UIView = {
    let view = UIView()
    view.backgroundColor = .white
    view.layer.masksToBounds = true
    view.layer.cornerRadius = 12
    return view
  }()

  lazy var textView: UITextView = {
    let view = UITextView(frame: self.view.frame)
    view.textContainerInset = .zero
    view.isEditable = false
    view.isScrollEnabled = false
    view.textAlignment = .center
    view.textColor = UIColor(red: 0.6, green: 0.6, blue: 0.6, alpha: 1)
    view.font = UIFont(name: "PingFangSC-Regular", size: 14) ?? UIFont.systemFont(ofSize: 14)

    view.text = content

    return view
  }()

  lazy var agreeButton: UIButton = {
    let btn = UIButton(frame: CGRect(x: 0, y: 0, width: 110, height: 44))
    btn.setTitleColor(.white, for: .normal)
    btn.setTitle(rightAction?.title, for: .normal)
    btn.setBackgroundImage(ne_chatUI_imageName(imageName: "alert_btn"), for: .normal)
    btn.addTarget(self, action: #selector(confirm(btn:)), for: .touchUpInside)
    btn.layer.cornerRadius = 22
    return btn
  }()

  lazy var disagreeButton: UIButton = {
    let btn = UIButton(frame: CGRect(x: 0, y: 0, width: 110, height: 44))
    btn.setTitleColor(UIColor(red: 0.133, green: 0.133, blue: 0.133, alpha: 1), for: .normal)
    btn.backgroundColor = UIColor(red: 0.945, green: 0.949, blue: 0.957, alpha: 1)
    btn.setTitle(leftAction?.title, for: .normal)
    btn.addTarget(self, action: #selector(confirm(btn:)), for: .touchUpInside)
    btn.layer.cornerRadius = 22
    return btn
  }()

  func updateViewConstrain(contentHeight: CGFloat) {
    var height: CGFloat = 20 + 25 // titleLabel
    height = height + 24 + contentHeight // textView
    height = height + 24 + 44 + 24 // button
    bgView.snp.updateConstraints { make in
      make.center.equalToSuperview()
      make.left.equalToSuperview().offset(53)
      make.right.equalToSuperview().offset(-53)
      make.height.equalTo(height)
    }

    titleLabel.snp.updateConstraints { make in
      make.top.equalTo(bgView).offset(20)
      make.left.right.equalTo(bgView)
      make.height.equalTo(25)
    }

    textView.snp.updateConstraints { make in
      make.top.equalTo(titleLabel.snp.bottom).offset(24)
      make.left.equalTo(bgView).offset(16)
      make.right.equalTo(bgView).offset(-16)
      make.height.equalTo(contentHeight)
    }

    let btnWidth = (view.bounds.width - 164) / 2 // - 53 * 2 - 24 * 2 - 10
    let width = min(btnWidth, 110)

    disagreeButton.snp.updateConstraints { make in
      make.left.equalToSuperview().offset(24)
      make.width.equalTo(width)
      make.height.equalTo(44)
      make.top.equalTo(textView.snp.bottom).offset(24)
    }

    agreeButton.snp.updateConstraints { make in
      make.right.equalToSuperview().offset(-24)
      make.width.equalTo(width)
      make.height.equalTo(44)
      make.top.equalTo(textView.snp.bottom).offset(24)
    }
  }

  @objc func confirm(btn: UIButton) {
    dismiss()
    if btn == agreeButton {
      rightAction?.handler?()
    } else if btn == disagreeButton {
      leftAction?.handler?()
    }
  }

  public func show(viewController: UIViewController) {
    viewController.present(self, animated: false)
  }

  public func dismiss() {
    dismiss(animated: false)
  }
}
