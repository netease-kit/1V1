// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

@objcMembers public class NEOneOnOneUserDetailHeaderCell: UITableViewCell {
  override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    selectionStyle = .none
    addViews()
    headerView.installData()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func addViews() {
    contentView.addSubview(headerView)
    headerView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(self.contentView)
    }
  }

  lazy var headerView: NEOneOnOneUserDetailHeaderView = {
    let headerView = NEOneOnOneUserDetailHeaderView()
    return headerView
  }()
}

@objcMembers public class NEOneOnOneUserDetailHeaderView: UIView {
  override public init(frame: CGRect) {
    super.init(frame: frame)
    addViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // TODO: 待处理
  public func installData() {
    headerImageView.sd_setImage(with: URL(string: "https://lmg.jj20.com/up/allimg/tp09/210Z614150050M-0-lp.jpg"))
    nameLabel.text = "名字"
    descriptionLabel.text = "这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长这段文字很长很长"
  }

  private func addViews() {
    layoutIfNeeded()
    addSubview(headerImageView)

    headerImageView.snp.makeConstraints { make in
      make.top.left.right.equalTo(self)
      make.height.equalTo(headerImageView.snp.width)
    }

    addSubview(backLabel)
    backLabel.snp.makeConstraints { make in
      make.left.equalTo(self).offset(20)
      make.bottom.equalTo(headerImageView.snp.bottom).offset(-14)
      make.height.equalTo(20)
      make.width.equalTo(60)
    }
    addSubview(onlineImageView)
    onlineImageView.snp.makeConstraints { make in
      make.left.equalTo(backLabel).offset(10)
      make.height.width.equalTo(9)
      make.centerY.equalTo(backLabel)
    }
    addSubview(onlineLabel)
    onlineLabel.snp.makeConstraints { make in
      make.centerY.equalTo(backLabel)
      make.right.equalTo(backLabel).offset(-10)
    }
    addSubview(nameLabel)
    nameLabel.snp.makeConstraints { make in
      make.left.equalTo(self).offset(20)
      make.right.equalTo(self).offset(-20)
      make.top.equalTo(headerImageView.snp.bottom).offset(30)
    }
    addSubview(descriptionLabel)
    descriptionLabel.snp.makeConstraints { make in
      make.left.equalTo(nameLabel)
      make.right.equalTo(self).offset(-20)
      make.top.equalTo(nameLabel.snp.bottom).offset(8)
      make.height.lessThanOrEqualTo(40)
    }

    addSubview(bottomLabel)
    bottomLabel.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(self)
      make.height.equalTo(6)
    }
  }

  // MARK: lazyLoad

  lazy var headerImageView: UIImageView = {
    let headerImageView = UIImageView()
    headerImageView.contentMode = .scaleAspectFill
    headerImageView.backgroundColor = .red
    return headerImageView
  }()

  lazy var onlineLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("在线")
    label.textColor = UIColor(hexString: "#FFFFFF")
    label.font = UIFont.systemFont(ofSize: 12)
    return label
  }()

  lazy var backLabel: UILabel = {
    let label = UILabel()
    label.text = ""
    label.backgroundColor = UIColor(hexString: "#000000", 0.5)
    label.layer.masksToBounds = true
    label.layer.cornerRadius = 10
    return label
  }()

  lazy var onlineImageView: UIImageView = {
    let imageView = UIImageView()
    imageView.image = ne_oneOnOne_imageName(imageName: "online_icon")
    imageView.contentMode = .center
    return imageView
  }()

  lazy var nameLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("Name")
    label.textColor = UIColor(hexString: "#000000")
    label.font = UIFont(name: "PingFangSC-Medium", size: 18)
    return label
  }()

  lazy var descriptionLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("描述")
    label.textColor = UIColor(hexString: "#999999")
    label.font = UIFont.systemFont(ofSize: 14)
    label.numberOfLines = 0
    return label
  }()

  lazy var bottomLabel: UILabel = {
    let label = UILabel()
    label.text = ""
    label.backgroundColor = UIColor(hexString: "#EFF1F4")
    return label
  }()
}

@objcMembers public class NEOneOnOneUserDetailBodyCell: UITableViewCell {
  public var title: String = "" {
    didSet {
      bodyView.titleLabel.text = title
    }
  }

  public var maxVisiableCount: Int = 4 {
    didSet {
      bodyView.maxVisiableCount = maxVisiableCount
    }
  }

  override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    selectionStyle = .none
    addViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func addViews() {
    contentView.addSubview(bodyView)
    bodyView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(self.contentView)
    }
  }

  lazy var bodyView: NEOneOnOneUserDetailBodyView = {
    let bodyView = NEOneOnOneUserDetailBodyView()
    return bodyView
  }()
}

@objcMembers public class NEOneOnOneUserDetailBodyView: UIView, UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
  /// 一次性可以看到的最大可视化个数
  public var maxVisiableCount = 4 {
    didSet {
      collectionView.reloadData()
    }
  }

  public var albumArrayList: Array = [Any]()

  override public init(frame: CGRect) {
    super.init(frame: frame)
    addViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func addViews() {
    addSubview(detailtView)
    detailtView.snp.makeConstraints { make in
      make.left.equalTo(self).offset(20)
      make.right.equalTo(self).offset(-20)
      make.top.equalTo(self).offset(24)
      make.bottom.equalTo(self).offset(-24)
    }
    detailtView.addSubview(titleLabel)
    titleLabel.snp.makeConstraints { make in
      make.left.top.equalTo(detailtView)
    }
    layoutIfNeeded()
    detailtView.addSubview(collectionView)
    collectionView.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(detailtView)
      make.top.equalTo(titleLabel.snp.bottom).offset(12)
    }
    addSubview(lineLabel)
    lineLabel.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(self)
      make.height.equalTo(1)
    }
  }

  // MARK: lazyLoad

  lazy var detailtView: UIView = {
    let detailtView = UIView()
    return detailtView
  }()

  lazy var lineLabel: UILabel = {
    let label = UILabel()
    label.text = ""
    label.backgroundColor = UIColor(hexString: "#F5F8FC")
    return label
  }()

  lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("相册")
    label.textColor = UIColor(hexString: "#000000")
    label.font = UIFont(name: "PingFangSC-Medium", size: 16)
    return label
  }()

  lazy var collectionView: UICollectionView = {
    let collectionViewLayout = MyCollectionViewFlowLayout()
    let collectionView = UICollectionView(frame: .zero, collectionViewLayout: collectionViewLayout)
    collectionView.delegate = self
    collectionView.dataSource = self
    collectionView.alwaysBounceVertical = true
    collectionView.backgroundColor = .white
    collectionView.register(NEOneOnOneUserDetailBodyCollectionCell.self, forCellWithReuseIdentifier: NEOneOnOneUserDetailBodyCollectionCell.description())
    if #available(iOS 11.0, *) {
      collectionView.contentInsetAdjustmentBehavior = .never
    }
    return collectionView
  }()

  // MARK: UICollectionViewDataSource

  public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    10
  }

  // MARK: UICollectionViewDelegateFlowLayout

  public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    if let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NEOneOnOneUserDetailBodyCollectionCell.description(), for: indexPath) as? NEOneOnOneUserDetailBodyCollectionCell {
      cell.backgroundColor = .green
      return cell
    }
    return NEOneOnOneUserDetailBodyCollectionCell()
  }

  public func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
    let collectionViewWidth = collectionView.bounds.width
    let itemSize = (Int(collectionViewWidth) - maxVisiableCount * 5) / maxVisiableCount
    return CGSize(width: itemSize, height: itemSize)
  }
}

class MyCollectionViewFlowLayout: UICollectionViewFlowLayout {
  override func prepare() {
    super.prepare()
    // 设置 cell 的其他布局属性
    scrollDirection = .horizontal
    minimumInteritemSpacing = 0
    minimumLineSpacing = 5
    sectionInset = UIEdgeInsets(top: 0, left: 2.5, bottom: 0, right: 2.5)
  }
}

@objcMembers public class NEOneOnOneUserDetailBodyCollectionCell: UICollectionViewCell {
  override public init(frame: CGRect) {
    super.init(frame: frame)
    contentView.addSubview(albumImageView)
    albumImageView.snp.makeConstraints { make in
      make.left.right.bottom.top.equalTo(contentView)
    }
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  // MARK: lazyLoad

  lazy var albumImageView: UIImageView = {
    let albumImageView = UIImageView()
    albumImageView.contentMode = .scaleAspectFit
    albumImageView.backgroundColor = .red
    albumImageView.layer.masksToBounds = true
    albumImageView.layer.cornerRadius = 10
    return albumImageView
  }()
}

@objcMembers public class NEOneOnOneUserDetailBottomCell: UITableViewCell {
  override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    selectionStyle = .none
    addViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func addViews() {
    contentView.addSubview(bottomView)
    bottomView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(self.contentView)
    }
  }

  lazy var bottomView: NEOneOnOneUserDetailBottomView = {
    let bottomView = NEOneOnOneUserDetailBottomView()
    return bottomView
  }()
}

@objcMembers public class NEOneOnOneUserDetailBottomView: UIView, UITableViewDelegate, UITableViewDataSource {
  override public init(frame: CGRect) {
    super.init(frame: frame)
    addViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  private func addViews() {
    addSubview(detailtView)
    detailtView.snp.makeConstraints { make in
      make.left.equalTo(self).offset(20)
      make.right.equalTo(self).offset(-20)
      make.top.equalTo(self).offset(24)
      make.bottom.equalTo(self).offset(-24)
    }

    detailtView.addSubview(titleLabel)
    titleLabel.snp.makeConstraints { make in
      make.left.top.equalTo(detailtView)
    }
    detailtView.addSubview(mainTableView)
    mainTableView.snp.makeConstraints { make in
      make.left.right.equalTo(detailtView)
      make.top.equalTo(titleLabel.snp.bottom)
      make.height.equalTo(200)
    }
  }

  // MARK: lazy Load

  public lazy var detailtView: UIView = {
    let detailtView = UIView()
    return detailtView
  }()

  lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("信息")
    label.textColor = UIColor(hexString: "#000000")
    label.font = UIFont(name: "PingFangSC-Medium", size: 16)
    return label
  }()

  public lazy var mainTableView: UITableView = {
    var tableView = UITableView()
    tableView.register(NEOneOnOneUserDetailBottomViewCell.self, forCellReuseIdentifier: NEOneOnOneUserDetailBottomViewCell.description())
    tableView.delegate = self
    tableView.dataSource = self
    tableView.backgroundColor = UIColor.red
    tableView.separatorStyle = .none
    let statusBarHeight = UIApplication.shared.statusBarFrame.height
    if #available(iOS 11.0, *) {
      tableView.contentInsetAdjustmentBehavior = .never
    } else {
      tableView.contentInset = UIEdgeInsets(top: statusBarHeight, left: 0, bottom: 0, right: 0)
    }
    return tableView
  }()

  // MARK: UITableViewDataSource

  public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    10
  }

  public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if let cell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneUserDetailBottomViewCell.description(), for: indexPath) as? NEOneOnOneUserDetailBottomViewCell {
      cell.titleLabel.text = "选项"
      cell.descLabel.text = "描述内容，有内容需要改色值为 666666"
      return cell
    }
    return NEOneOnOneUserDetailBottomViewCell()
  }

  public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    50
  }
}

@objcMembers public class NEOneOnOneUserDetailBottomViewCell: UITableViewCell {
  override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    selectionStyle = .none
    addViews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func addViews() {
    contentView.addSubview(titleLabel)
    titleLabel.snp.makeConstraints { make in
      make.left.equalTo(contentView)
      make.centerY.equalTo(contentView)
      make.width.lessThanOrEqualTo(contentView.width / 2)
    }

    contentView.addSubview(descLabel)
    descLabel.snp.makeConstraints { make in
      make.right.equalTo(contentView)
      make.centerY.equalTo(contentView)
      make.width.lessThanOrEqualTo(contentView.width / 2)
    }

    contentView.addSubview(lineLabel)
    lineLabel.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(contentView)
      make.height.equalTo(1)
    }
  }

  public lazy var titleLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("title")
    label.textColor = UIColor(hexString: "#666666")
    label.font = UIFont(name: "PingFangSC", size: 14)
    return label
  }()

  public lazy var descLabel: UILabel = {
    let label = UILabel()
    label.text = ne_oneOnOne_localized("未填写")
    label.textColor = UIColor(hexString: "#C4C4C4")
    label.font = UIFont(name: "PingFangSC", size: 14)
    return label
  }()

  lazy var lineLabel: UILabel = {
    let label = UILabel()
    label.text = ""
    label.backgroundColor = UIColor(hexString: "#F5F8FC")
    return label
  }()
}
