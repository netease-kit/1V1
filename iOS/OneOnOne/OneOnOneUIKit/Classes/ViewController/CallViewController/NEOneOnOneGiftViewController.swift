// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NECommonUIKit
import SnapKit
import UIKit

class NEOneOnOneGiftUtil: NSObject {
  class func ne_localBundle() -> Bundle {
    Bundle(for: NEOneOnOneGiftUtil.self)
  }
}

func ne_oneOnOne_localized(_ key: String) -> String {
  NEOneOnOneGiftUtil.ne_localBundle().localizedString(forKey: key, value: nil, table: "Localizable")
}

func ne_oneOnOne_imageName(imageName: String) -> UIImage? {
  if #available(iOS 13.0, *) {
    return UIImage(named: imageName, in: NEOneOnOneGiftUtil.ne_localBundle(), with: nil)
  } else {
    return UIImage(named: imageName, in: NEOneOnOneGiftUtil.ne_localBundle(), compatibleWith: nil)
  }
}

@objc public protocol NEOneOnOneGiftViewDelegate: NSObjectProtocol {
  @objc optional func giftView(_ giftView: NEOneOnOneGiftViewController, sendGift gift: NEOneOnOneGiftItem, count: Int)
}

@objcMembers public class NEOneOnOneGiftItem: NSObject {
  public var giftId: Int
  public var imageName: String
  public var displayName: String
  public var price: Int

  init(giftId: Int, imageName: String, displayName: String, price: Int) {
    self.giftId = giftId
    self.imageName = imageName
    self.displayName = displayName
    self.price = price
  }
}

public class NEOneOnOneGiftCountCell: UITableViewCell {
  override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    contentView.backgroundColor = .clear
    contentView.addSubview(contentLabel)
    selectionStyle = .none
    contentLabel.snp.makeConstraints { make in
      make.width.equalTo(96)
      make.height.equalTo(28)
      make.centerY.centerX.equalToSuperview()
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private var _customSelected = false
  public var customSelected: Bool {
    get {
      _customSelected
    }
    set {
      _customSelected = newValue
      if newValue {
        contentLabel.layer.borderWidth = 1
        contentLabel.backgroundColor = UIColor(hexString: "#F9627C", 0.02)
        contentLabel.textColor = UIColor(hexString: "#FA667C")
      } else {
        contentLabel.layer.borderWidth = 0
        contentLabel.backgroundColor = .clear
        contentLabel.textColor = UIColor(hexString: "#333333")
      }
    }
  }

  public lazy var contentLabel: UILabel = {
    var label = UILabel()
    label.textColor = UIColor(hexString: "#333333")
    label.font = UIFont(name: "PingFangSC-Regular", size: 14)
    label.textAlignment = .center
    label.layer.cornerRadius = 14
    label.clipsToBounds = true
    label.layer.borderColor = UIColor(hexString: "#FA667C").cgColor
    label.accessibilityIdentifier = "id.couldSelectCountText"
    return label
  }()
}

public class NEOneOnOneGiftCell: UICollectionViewCell {
  class func size() -> CGSize {
    // 一排放4个
    CGSize(width: (UIScreen.main.bounds.width - 30) / 4, height: 136)
  }

  override public init(frame: CGRect) {
    super.init(frame: frame)
    contentView.backgroundColor = .white
    contentView.addSubview(bgView)
    bgView.addSubview(icon)
    bgView.addSubview(displayNameLabel)

    bgView.snp.makeConstraints { make in
      make.left.equalToSuperview().offset(4)
      make.right.equalToSuperview().offset(-4)
      make.top.equalToSuperview().offset(20)
      make.height.equalTo(88)
    }

    icon.snp.makeConstraints { make in
      make.centerX.equalToSuperview()
      make.width.height.equalTo(40)
      make.top.equalToSuperview().offset(8)
    }

    displayNameLabel.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.top.equalTo(icon.snp.bottom).offset(4)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override public var isSelected: Bool {
    get {
      super.isSelected
    }
    set {
      if newValue {
        bgView.layer.borderWidth = 1
        bgView.backgroundColor = UIColor(hexString: "#F9627C", 0.02)
      } else {
        bgView.layer.borderWidth = 0
        bgView.backgroundColor = .clear
      }
    }
  }

  public func setup(item: NEOneOnOneGiftItem) {
    icon.image = ne_oneOnOne_imageName(imageName: item.imageName)
    displayNameLabel.text = item.displayName
  }

  lazy var icon: UIImageView = {
    let icon = UIImageView()
    icon.accessibilityIdentifier = "id.ivItemGiftIcon"
    return icon
  }()

  lazy var displayNameLabel: UILabel = {
    let label = UILabel()
    label.textColor = UIColor(hexString: "#333333")
    label.font = UIFont(name: "PingFangSC-Regular", size: 14)
    label.textAlignment = .center
    label.accessibilityIdentifier = "id.tvItemGiftName"
    return label
  }()

  lazy var bgView: UIView = {
    let view = UIView()
    view.layer.cornerRadius = 4
    view.clipsToBounds = true
    view.layer.borderColor = UIColor(hexString: "#FA667C").cgColor
    return view
  }()
}

@objcMembers public class NEOneOnOneGiftViewController: UIViewController {
  weak var delegate: NEOneOnOneGiftViewDelegate?

  public static func show(viewController: UIViewController, delegate: NEOneOnOneGiftViewDelegate? = nil) {
    let giftView = NEOneOnOneGiftViewController()
    giftView.delegate = delegate
    let actionSheet = NEActionSheetController(rootViewController: giftView)
    actionSheet.dismissOnTouchOutside = true
    viewController.present(actionSheet, animated: true)
  }

  override public func viewDidLoad() {
    super.viewDidLoad()
    title = ne_oneOnOne_localized("礼物")
    navigationItem.titleView?.accessibilityIdentifier = "id.giftTitle"
    view.backgroundColor = .white
    view.addSubview(bottomSendGiftView)
    view.addSubview(collectionView)

    collectionView.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.top.equalToSuperview().offset(48 + 16)
      make.height.equalTo(136)
    }

    bottomSendGiftView.snp.makeConstraints { make in
      make.top.equalTo(collectionView.snp.bottom).offset(16)
      make.right.equalToSuperview().offset(-12)
      make.height.equalTo(32)
      make.width.equalTo(140)
    }
  }

  lazy var collectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    layout.itemSize = NEOneOnOneGiftCell.size()
    layout.scrollDirection = .horizontal
    layout.minimumInteritemSpacing = 5
    layout.minimumLineSpacing = 5

    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
    collectionView.backgroundColor = .clear
    collectionView.delegate = self
    collectionView.dataSource = self
    collectionView.showsHorizontalScrollIndicator = false
    collectionView.isScrollEnabled = false
    collectionView.register(NEOneOnOneGiftCell.self, forCellWithReuseIdentifier: NEOneOnOneGiftCell.description())
    collectionView.allowsMultipleSelection = false
    collectionView.selectItem(at: IndexPath(row: 0, section: 0), animated: false, scrollPosition: .top)
    return collectionView
  }()

  lazy var bottomSendGiftView: UIView = {
    let view = UIView(frame: CGRect(x: 0, y: 200, width: 140, height: 32))
    view.backgroundColor = UIColor(hexString: "#EDEFF2")
    view.layer.cornerRadius = 14
    view.clipsToBounds = true
    view.addSubview(bottomSendGiftButton)
    view.addSubview(bottomGiftCountButton)
    bottomSendGiftButton.snp.makeConstraints { make in
      make.top.equalToSuperview().offset(1)
      make.bottom.equalToSuperview().offset(-1)
      make.right.equalToSuperview().offset(-1)
      make.width.equalTo(60)
    }
    bottomGiftCountButton.snp.makeConstraints { make in
      make.left.equalToSuperview()
      make.right.equalTo(bottomSendGiftButton.snp.left)
      make.top.bottom.equalToSuperview()
    }
    return view
  }()

  func sendGift() {
    if let delegate = delegate,
       delegate.responds(to: #selector(NEOneOnOneGiftViewDelegate.giftView(_:sendGift:count:))),
       let items = collectionView.indexPathsForSelectedItems,
       items.count == 1 {
      delegate.giftView?(self, sendGift: dataSource[items[0].row], count: selectCount)
    }
    navigationController?.dismiss(animated: true)
  }

  lazy var bottomSendGiftButton: UIButton = {
    let view = UIButton(frame: CGRect(x: 0, y: 0, width: 60, height: 30))
    view.setTitle(ne_oneOnOne_localized("赠送"), for: .normal)
    view.setTitleColor(UIColor.white, for: .normal)
    view.clipsToBounds = true
    view.layer.cornerRadius = 14
    view.setBackgroundImage(ne_oneOnOne_imageName(imageName: "alert_btn"), for: .normal)
    view.setTitleColor(UIColor(white: 1, alpha: 0.5), for: .disabled)
    view.addTarget(self, action: #selector(sendGift), for: .touchUpInside)
    view.accessibilityIdentifier = "id.giftSendBtn"
    return view
  }()

  lazy var bottomGiftCountButton: UIButton = {
    let view = UIButton(frame: CGRect(x: 0, y: 0, width: 60, height: 30))
    view.setTitle("1", for: .normal)
    view.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    view.setImage(ne_oneOnOne_imageName(imageName: "gift_up"), for: .normal)
    view.setImage(ne_oneOnOne_imageName(imageName: "gift_down"), for: .selected)
    view.addTarget(self, action: #selector(selectGiftCount(sender:)), for: .touchUpInside)
    view.imageEdgeInsets = UIEdgeInsets(top: 0, left: -6, bottom: 0, right: 6)
    view.titleEdgeInsets = UIEdgeInsets(top: 0, left: 6, bottom: 0, right: -6)
    view.accessibilityIdentifier = "id.sendGiftCountBtn"
    return view
  }()

  func selectGiftCount(sender: UIButton) {
    sender.isSelected = !sender.isSelected
    if sender.isSelected {
      // 弹出popup
      popover.show(popoverView, fromView: bottomSendGiftView)
    } else {
      // 收回popup
      popover.dismiss()
    }
  }

  lazy var popover: Popover = {
    let options = [
      .blackOverlayColor(.clear),
      .cornerRadius(4),
      .dismissOnBlackOverlayTap(true),
      .type(.up),
      .animationIn(0.5),
      .arrowSize(CGSize.zero),
      .color(.clear),
    ] as [PopoverOption]
    let popover = Popover(options: options)
    popover.willDismissHandler = { [weak self] in
      self?.bottomGiftCountButton.isSelected = false
    }
    return popover
  }()

  lazy var popoverView: UIView = {
    let view = UIView(frame: CGRect(x: 0, y: 0, width: 140, height: 250))
    view.backgroundColor = .clear
    let backImage = UIImageView(image: ne_oneOnOne_imageName(imageName: "gift_back"))
    view.addSubview(backImage)
    view.addSubview(tableView)

    backImage.snp.makeConstraints { make in
      make.top.left.width.equalToSuperview()
      make.bottom.equalToSuperview().offset(20)
    }
    tableView.snp.makeConstraints { make in
      make.width.equalTo(100)
      make.centerY.centerX.equalToSuperview()
      make.height.equalTo(giftCounts.count * 35)
    }
    return view
  }()

  lazy var tableView: UITableView = {
    let giftTableView = UITableView(frame: CGRect(x: 0, y: 0, width: 120, height: 232))
    giftTableView.backgroundColor = .white
    giftTableView.separatorStyle = .none
    giftTableView.delegate = self
    giftTableView.dataSource = self
    giftTableView.allowsMultipleSelection = false
    giftTableView.isScrollEnabled = false
    giftTableView.register(NEOneOnOneGiftCountCell.self, forCellReuseIdentifier: NEOneOnOneGiftCountCell.description())
    return giftTableView
  }()

  var giftCounts = [1314, 520, 66, 20, 6, 1]
  var selectCount = 1

  lazy var dataSource: [NEOneOnOneGiftItem] = {
    var array = [NEOneOnOneGiftItem]()
    array.append(NEOneOnOneGiftItem(giftId: 1, imageName: "gift01_ico", displayName: ne_oneOnOne_localized("荧光棒"), price: 9))
    array.append(NEOneOnOneGiftItem(giftId: 2, imageName: "gift02_ico", displayName: ne_oneOnOne_localized("安排"), price: 99))
    array.append(NEOneOnOneGiftItem(giftId: 3, imageName: "gift03_ico", displayName: ne_oneOnOne_localized("跑车"), price: 199))
    array.append(NEOneOnOneGiftItem(giftId: 4, imageName: "gift04_ico", displayName: ne_oneOnOne_localized("火箭"), price: 999))
    return array
  }()

  override public var preferredContentSize: CGSize {
    get {
      var total: CGFloat = 226
      if #available(iOS 11.0, *) {
        total += UIApplication.shared.keyWindow?.rootViewController?.view.safeAreaInsets.bottom ?? 0
      }
      return CGSize(width: UIScreen.main.bounds.width, height: total)
    }
    set {
      super.preferredContentSize = newValue
    }
  }
}

extension NEOneOnOneGiftViewController: UICollectionViewDelegate {
  public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
    if dataSource.count > indexPath.row {
      bottomSendGiftButton.isEnabled = true
    }
    collectionView.selectItem(at: indexPath, animated: true, scrollPosition: .top)
  }
}

extension NEOneOnOneGiftViewController: UICollectionViewDataSource {
  public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    dataSource.count
  }

  public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NEOneOnOneGiftCell.description(), for: indexPath)
    if dataSource.count > indexPath.row,
       let cell = cell as? NEOneOnOneGiftCell {
      let gift = dataSource[indexPath.row]
      cell.setup(item: gift)
    }
    return cell
  }
}

extension NEOneOnOneGiftViewController: UITableViewDelegate {
  public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
    bottomGiftCountButton.setTitle(String(giftCounts[indexPath.row]), for: .normal)
    selectCount = giftCounts[indexPath.row]
    tableView.reloadData()
    popover.dismiss()
  }
}

extension NEOneOnOneGiftViewController: UITableViewDataSource {
  public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let isSelected = giftCounts[indexPath.row] == selectCount
    if let cell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneGiftCountCell.description()) as? NEOneOnOneGiftCountCell {
      cell.contentLabel.text = String(giftCounts[indexPath.row])
      cell.customSelected = isSelected
      return cell
    } else {
      let cell = NEOneOnOneGiftCountCell(style: .default, reuseIdentifier: NEOneOnOneGiftCountCell.description())
      cell.contentLabel.text = String(giftCounts[indexPath.row])
      cell.customSelected = isSelected
      return cell
    }
  }

  public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    giftCounts.count
  }

  public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    35
  }
}
