// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NECommonUIKit

@objcMembers
public class NEOneOnOneReportView: UIView {
  public typealias CompletionCallback = (Int) -> Void
  var selectReportCallback: CompletionCallback?
  /// Y position 用于做动画
  var showYPositon = 0.0

  /// 内部数据源
  var _reportArray: [String] = .init()

  var reportArray: [String] {
    get {
      _reportArray
    }
    set {
      _reportArray = newValue
      reportTableView.reloadData()
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
      backView.heightAnchor.constraint(equalToConstant: 292),
    ])

    addSubview(reportTableView)
    NSLayoutConstraint.activate([
      reportTableView.topAnchor.constraint(equalTo: backView.topAnchor),
      reportTableView.leftAnchor.constraint(equalTo: leftAnchor),
      reportTableView.rightAnchor.constraint(equalTo: rightAnchor),
      reportTableView.heightAnchor.constraint(equalToConstant: 200),

    ])

    addSubview(lineView)
    lineView.snp.makeConstraints { make in
      make.left.right.equalTo(backView)
      make.top.equalTo(reportTableView.snp.bottom)
      make.height.equalTo(8)
    }
    addSubview(cancelButton)
    cancelButton.snp.makeConstraints { make in
      make.top.equalTo(lineView.snp.bottom)
      make.left.right.equalTo(backView)
      make.height.equalTo(48)
    }
  }

  // MARK: lazyLoad

  lazy var lineView: UIView = {
    let lineView = UIView()
    lineView.backgroundColor = UIColor(hexString: "F0F0F2")
    return lineView
  }()

  lazy var reportTableView: UITableView = {
    let reportTableView = UITableView()
    reportTableView.translatesAutoresizingMaskIntoConstraints = false
    reportTableView.delegate = self
    reportTableView.dataSource = self
    reportTableView.register(NEOneOnOneReportCell.self, forCellReuseIdentifier: "reportTableViewIdentifier")
    reportTableView.backgroundColor = .clear
    reportTableView.separatorStyle = .none
    return reportTableView
  }()

  lazy var cancelButton: UIButton = {
    let cancelButton = UIButton()
    cancelButton.setTitle(ne_oneOnOne_localized("取消"), for: .normal)
    cancelButton.setTitleColor(UIColor.black, for: .normal)
    cancelButton.setTitleColor(UIColor.black, for: .highlighted)
    cancelButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 16)
    cancelButton.addTarget(self, action: #selector(clickCancelButton), for: .touchUpInside)
    return cancelButton
  }()

  lazy var backView: UIView = {
    let backView = UIView(frame: CGRectMake(0, 0, UIScreen.main.bounds.self.width, UIScreen.main.bounds.self.height))
    backView.translatesAutoresizingMaskIntoConstraints = false
    setCornersRadius(backView, radius: 5.0, roundingCorners: [[.topLeft, .topRight]])
    backView.backgroundColor = UIColor.white
    backView.layer.masksToBounds = true

    return backView
  }()

  func clickCancelButton() {
    if let selectReportCallback = selectReportCallback {
      selectReportCallback(reportArray.count)
      dismissView(nil)
    }
  }

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

extension NEOneOnOneReportView: UITableViewDelegate, UITableViewDataSource {
  public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    reportArray.count
  }

  public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if let cell = tableView.dequeueReusableCell(withIdentifier: "reportTableViewIdentifier", for: indexPath) as? NEOneOnOneReportCell {
      cell.contentLabel.text = _reportArray[indexPath.row]
      return cell
    } else {
      return UITableViewCell()
    }
  }

  public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    50
  }

  public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    if _reportArray.count > indexPath.row {
      if let selectReportCallback = selectReportCallback {
        selectReportCallback(indexPath.row)
        dismissView(nil)
      }
    }
  }
}

class NEOneOnOneReportCell: UITableViewCell {
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
    addSubview(contentLabel)
    NSLayoutConstraint.activate([
      contentLabel.leftAnchor.constraint(equalTo: leftAnchor, constant: 16),
      contentLabel.centerYAnchor.constraint(equalTo: centerYAnchor, constant: 0),
      contentLabel.rightAnchor.constraint(equalTo: rightAnchor, constant: -16),
      contentLabel.heightAnchor.constraint(equalToConstant: 50),
    ]
    )
  }

  lazy var contentLabel = {
    let label = UILabel()
    label.translatesAutoresizingMaskIntoConstraints = false
    label.font = UIFont(name: "PingFangSC-Regular", size: 16)
    label.textColor = UIColor(hexString: "#333333")
    label.textAlignment = .center
    return label
  }()
}
