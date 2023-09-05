// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

public class NEOneOnOnePopView: UIView {
  public var reportAction: (() -> Void)?
  public var blockAction: (() -> Void)?

  private var _isInBlack = false
  public var isInBlack: Bool {
    set {
      blockButton.setTitle(newValue ? ne_oneOnOne_localized("取消拉黑") : ne_oneOnOne_localized("拉黑"), for: .normal)
      _isInBlack = newValue
    }
    get {
      _isInBlack
    }
  }

  public var sourceRect: CGRect = CGRectZero {
    didSet {
      updateViews()
    }
  }

  override public init(frame: CGRect) {
    super.init(frame: frame)
    layer.backgroundColor = UIColor.clear.cgColor
    reportBackImageView.backgroundColor = UIColor.clear
    addSubview(reportBackImageView)
    reportBackImageView.snp.makeConstraints { make in
      make.top.equalTo(0)
      make.right.equalTo(0)
      make.width.equalTo(100)
      make.height.equalTo(80)
    }

    addSubview(reportButton)
    reportButton.snp.makeConstraints { make in
      make.centerX.equalTo(reportBackImageView.snp.centerX)
      make.top.equalTo(reportBackImageView).offset(11)
      make.width.equalTo(reportBackImageView)
    }

    addSubview(blockButton)
    blockButton.snp.makeConstraints { make in
      make.centerX.equalTo(reportBackImageView.snp.centerX)
      make.top.equalTo(reportButton.snp.bottom).offset(3)
      make.width.equalTo(reportBackImageView)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var reportButton: UIButton = {
    let reportButton = UIButton()
    reportButton.setTitle(ne_oneOnOne_localized("举报"), for: UIControl.State.normal)
    reportButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    reportButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    reportButton.addTarget(self, action: #selector(clickReportButton), for: .touchUpInside)
    return reportButton
  }()

  lazy var blockButton: UIButton = {
    let blockButton = UIButton()
    blockButton.setTitle(_isInBlack ? ne_oneOnOne_localized("取消拉黑") : ne_oneOnOne_localized("拉黑"), for: UIControl.State.normal)
    blockButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    blockButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    blockButton.addTarget(self, action: #selector(clickBlockButton), for: .touchUpInside)
    return blockButton
  }()

  @objc func clickReportButton() {
    if let reportAction = reportAction {
      reportAction()
    }
    removeFromSuperview()
  }

  @objc func clickBlockButton() {
    if let blockAction = blockAction {
      blockAction()
    }
    removeFromSuperview()
  }

  lazy var reportBackImageView: UIImageView = {
    let reportBackImageView = UIImageView()
    reportBackImageView.image = ne_oneOnOne_imageName(imageName: "report_back_icon")
    reportBackImageView.contentMode = .scaleToFill
    reportBackImageView.backgroundColor = UIColor.clear
    return reportBackImageView
  }()

  private func updateViews() {
    reportBackImageView.snp.updateConstraints { make in
      make.top.equalTo(sourceRect.origin.y)
      make.right.equalTo(self).offset(-10)
      make.width.equalTo(100)
      make.height.equalTo(80)
    }
  }

  public func show() {
    UIApplication.shared.keyWindow?.addSubview(self)
  }

  override public func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
    removeFromSuperview()
  }
}
