// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NEOneOnOneKit

class NEOneOnOneAssistantHeaderView: UIView {
  /// 左上角返回按钮 block
  var leftBackAction: (() -> Void)?
  /// 右上角X按钮 block
  var closeAction: (() -> Void)?
  /// 右上角Home按钮 block
  var homeAction: (() -> Void)?

  override init(frame: CGRect) {
    super.init(frame: frame)
    loadSubviews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubviews() {
    backgroundColor = UIColor(hexString: "eef5fe")

    addSubview(headerTitleLabel)
    addSubview(backButton)
    addSubview(closeButton)
    addSubview(homeButton)

    headerTitleLabel.snp.makeConstraints { make in
      make.centerY.equalTo(self)
      make.left.equalTo(self).offset(75)
      make.right.equalTo(self).offset(-75)
    }
    backButton.snp.makeConstraints { make in
      make.centerY.equalTo(headerTitleLabel)
      make.left.equalTo(self).offset(19)
      make.width.height.equalTo(25)
    }

    closeButton.snp.makeConstraints { make in
      make.centerY.equalTo(headerTitleLabel)
      make.right.equalTo(self).offset(-19)
      make.width.height.equalTo(25)
    }

    homeButton.snp.makeConstraints { make in
      make.centerY.equalTo(headerTitleLabel)
      make.right.equalTo(closeButton.snp.left).offset(-19)
      make.width.height.equalTo(25)
    }
  }

  // MARK: lazyLoad

  lazy var backButton: UIButton = {
    let backButton = UIButton()
    backButton.setImage(ne_chatUI_imageName(imageName: "assistant_back_icon"), for: .normal)
    backButton.addTarget(self, action: #selector(clickBackButton), for: .touchUpInside)
    return backButton
  }()

  lazy var headerTitleLabel: UILabel = {
    let headerTitle = UILabel()
    headerTitle.font = UIFont(name: "PingFangSC-Medium", size: 16)
    headerTitle.textAlignment = .center
    headerTitle.textColor = UIColor(hexString: "#222222")
    headerTitle.text = "Titile"
    return headerTitle
  }()

  lazy var homeButton: UIButton = {
    let homeButton = UIButton()
    homeButton.setImage(ne_chatUI_imageName(imageName: "assistant_home_icon"), for: .normal)
    homeButton.addTarget(self, action: #selector(clickHomeButton), for: .touchUpInside)
    return homeButton
  }()

  lazy var closeButton: UIButton = {
    let closeButton = UIButton()
    closeButton.setImage(ne_chatUI_imageName(imageName: "assistant_close_icon"), for: .normal)
    closeButton.addTarget(self, action: #selector(clickCloseButton), for: .touchUpInside)
    return closeButton
  }()

  // MARK: actionSelector

  @objc func clickBackButton() {
    leftBackAction?()
  }

  @objc func clickHomeButton() {
    homeAction?()
  }

  @objc func clickCloseButton() {
    closeAction?()
  }
}

class NEOneOnOneAssistantView: UIView {
  /// 左上角返回按钮 block
  var leftBackAction: (() -> Void)?
  /// 右上角X按钮 block
  var closeAction: (() -> Void)?
  /// 右上角Home按钮 block
  var homeAction: (() -> Void)?
  /// 刷新 block
  var refreshAction: (() -> Void)?
  override init(frame: CGRect) {
    super.init(frame: frame)
    loadSubviews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func refresh() {
    refreshAction?()
  }

  func loadSubviews() {
    backgroundColor = UIColor(hexString: "eef5fe")
    if #available(iOS 11.0, *) {
      layer.cornerRadius = 16
      layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
      clipsToBounds = true
    }

    addSubview(headerView)

    headerView.leftBackAction = { [weak self] in
      self?.leftBackAction?()
    }
    headerView.closeAction = { [weak self] in
      self?.closeAction?()
    }
    headerView.homeAction = { [weak self] in
      self?.homeAction?()
    }

    headerView.snp.makeConstraints { make in
      make.left.right.top.equalTo(self)
      make.height.equalTo(50)
    }

    addSubview(descriptionLabel)
    descriptionLabel.snp.makeConstraints { make in
      make.left.equalTo(self).offset(17)
      make.right.equalTo(self).offset(-17)
      make.top.equalTo(headerView.snp.bottom)
    }

    addSubview(mainView)
    mainView.snp.makeConstraints { make in
      make.left.right.bottom.equalTo(self)
      make.top.equalTo(descriptionLabel.snp.bottom)
    }

    mainView.addSubview(refreshButton)
    refreshButton.snp.makeConstraints { make in
      make.left.equalTo(mainView).offset(12)
      make.right.equalTo(mainView).offset(-12)
      make.bottom.equalTo(mainView.snp.bottom).offset(-20)
      make.height.equalTo(45)
    }
    mainView.addSubview(contentView)
    contentView.snp.makeConstraints { make in
      make.top.equalTo(mainView)
      make.bottom.equalTo(refreshButton.snp.top).offset(-5)
      make.left.right.equalTo(mainView)
    }

    addSubview(loadingView)
    loadingView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(self)
    }

    loadingView.addSubview(loadingImageView)
    loadingImageView.snp.makeConstraints { make in
      make.centerX.equalTo(loadingView)
      make.centerY.equalTo(loadingView.snp.centerY).offset(-30)
      make.height.width.equalTo(42)
    }

    loadingView.addSubview(loadingLabel)
    loadingLabel.snp.makeConstraints { make in
      make.centerX.equalTo(loadingView)
      make.top.equalTo(loadingImageView.snp.bottom).offset(14)
    }
    loadingView.isHidden = true
  }

  // MARK: lazyLoad

  lazy var headerView: NEOneOnOneAssistantHeaderView = {
    let header = NEOneOnOneAssistantHeaderView()
    return header
  }()

  lazy var descriptionLabel: UILabel = {
    let descriptionLabel = UILabel()
    descriptionLabel.font = UIFont(name: "PingFangSC-Regular", size: 12)
    descriptionLabel.textColor = UIColor(hexString: "#337EFF")
    descriptionLabel.textAlignment = .left
    descriptionLabel.text = ne_localized("以下是给您推荐的开场话术，可直接复制到输入框")
    descriptionLabel.numberOfLines = 0
    return descriptionLabel
  }()

  // 主视图
  lazy var mainView: UIView = {
    let mainView = UIView()
    mainView.backgroundColor = UIColor(hexString: "eef5fe")
    return mainView
  }()

  // 待填充内容视图
  lazy var contentView: UIView = {
    let contentView = UIView()
    contentView.backgroundColor = UIColor(hexString: "eef5fe")
    return contentView
  }()

  /// 加载页父视图
  lazy var loadingView: UIView = {
    let loadingView = UIView()
    loadingView.backgroundColor = UIColor(hexString: "#FFFFFF", 0.8)
    return loadingView
  }()

  /// 加载图
  lazy var loadingImageView: UIImageView = {
    let loadingImageView = UIImageView(image: ne_chatUI_imageName(imageName: "assistant_spiner_icon"))
    return loadingImageView
  }()

  /// loading动画
  lazy var rotationAnimation: CABasicAnimation = {
    let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
    rotationAnimation.toValue = NSNumber(value: Double.pi * 2.0)
    rotationAnimation.duration = 1.0
    rotationAnimation.isCumulative = true
    rotationAnimation.repeatCount = Float.greatestFiniteMagnitude
    return rotationAnimation
  }()

  lazy var loadingLabel: UILabel = {
    let loadingLabel = UILabel()
    loadingLabel.text = ne_localized("请稍等哦，正在获取中...")
    loadingLabel.font = UIFont(name: "PingFangSC-Regular", size: 14)
    return loadingLabel
  }()

  /// 换一批按钮
  lazy var refreshButton: UIButton = {
    let refreshButton = UIButton()
    refreshButton.setTitle(ne_localized("换一批"), for: .normal)
    refreshButton.setTitleColor(UIColor(hexString: "#FFFFFF"), for: .normal)
    refreshButton.titleLabel?.font = UIFont(name: "PingFangSC-Medium", size: 16)
    let gradientColors: [CGColor] = [UIColor(hexString: "#F9657B").cgColor, UIColor(hexString: "#FE7C74").cgColor]
    let gradientLayer = CAGradientLayer()
    gradientLayer.frame = CGRectMake(0, 0, 500, 50)
    gradientLayer.colors = gradientColors
    gradientLayer.startPoint = CGPointMake(0, 0.5)
    gradientLayer.endPoint = CGPointMake(1, 0.5)
    refreshButton.layer.masksToBounds = true
    refreshButton.layer.cornerRadius = 22.5
    refreshButton.layer.addSublayer(gradientLayer)
    refreshButton.addTarget(self, action: #selector(clickRefreshButton), for: .touchUpInside)
    if let imageView = refreshButton.imageView {
      refreshButton.bringSubviewToFront(imageView)
    }
    if let titleView = refreshButton.titleLabel {
      refreshButton.bringSubviewToFront(titleView)
    }
    return refreshButton
  }()

  // MARK: actionSelector

  @objc func clickRefreshButton() {
    refreshAction?()
  }

  func showLoadingView() {
    DispatchQueue.main.async { [weak self] in
      self?.loadingView.isHidden = false
      if let animation = self?.rotationAnimation {
        self?.loadingImageView.layer.add(animation, forKey: "rotationAnimation")
      }
    }
  }

  func hideLoadingView() {
    DispatchQueue.main.async { [weak self] in
      self?.loadingView.isHidden = true
      self?.loadingImageView.layer.removeAllAnimations()
    }
  }
}

class NEOneOnOneAssistantTopicView: UIView, UICollectionViewDelegate, UICollectionViewDataSource {
  let colorSourceData =
    [["boardColor": "#60CFA7", "backGroundColor": "#DFFFF4", "textColor": "#60CFA7"],
     ["boardColor": "#53C3F3", "backGroundColor": "#D6F3FF", "textColor": "#53C3F3"],
     ["boardColor": "#8AB9FF", "backGroundColor": "#CBE0FF", "textColor": "#4788E9"],
     ["boardColor": "#5879EF", "backGroundColor": "#D7DFFA", "textColor": "#5879EF"],
     ["boardColor": "#9A58EF", "backGroundColor": "#E7DBF6", "textColor": "#9A58EF"],
     ["boardColor": "#FF40D5", "backGroundColor": "#FFD4F6", "textColor": "#FF40D5"],
     ["boardColor": "#FF4057", "backGroundColor": "#FFC9CF", "textColor": "#FF4057"],
     ["boardColor": "#FF5C00", "backGroundColor": "#FFEBE0", "textColor": "#FF5C00"],
     ["boardColor": "#FFB800", "backGroundColor": "#FFF4D9", "textColor": "#FFB800"]]

  /// 数据源
  var sourceData: [NEOneOnOneAIGCTopicExtention] = []
  /// 左上角返回按钮 block
  var leftBackAction: (() -> Void)?
  /// 右上角X按钮 block
  var closeAction: (() -> Void)?
  /// 右上角Home按钮 block
  var homeAction: (() -> Void)?
  /// 点击话题
  var itemClickAction: ((Int, NEOneOnOneAIGCTopic?) -> Void)?

  override init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = UIColor(hexString: "eef5fe")
    NEOneOnOneKit.getInstance().getTopic { [weak self] code, msg, topicData in
      if code == NEOneOnOneErrorCode.success {
        if let topicData = topicData, let self = self {
          self.makeDataSource(topicData)
        }
      }
    }
    loadSubviews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func makeDataSource(_ topicData: [NEOneOnOneAIGCTopic]) {
    var tempSourceData: [NEOneOnOneAIGCTopicExtention] = []
    for item in topicData {
      let topicItem = NEOneOnOneAIGCTopicExtention()
      // 生成随机数
      let randomInt = Int(arc4random_uniform(9))
      topicItem.topic = item
      topicItem.boardColor = colorSourceData[randomInt]["boardColor"]
      topicItem.backGroundColor = colorSourceData[randomInt]["backGroundColor"]
      topicItem.textColor = colorSourceData[randomInt]["textColor"]
      tempSourceData.append(topicItem)
    }
    DispatchQueue.main.async { [weak self] in
      self?.sourceData.removeAll()
      self?.mainCollectionView.reloadData()
      do {
        let encodedData = try JSONEncoder().encode(tempSourceData)
        self?.sourceData = try JSONDecoder().decode([NEOneOnOneAIGCTopicExtention].self, from: encodedData)
        self?.mainCollectionView.reloadData()

      } catch {}
    }
  }

  func loadSubviews() {
    if #available(iOS 11.0, *) {
      layer.cornerRadius = 16
      layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
      clipsToBounds = true
    }

    addSubview(headerView)
    headerView.headerTitleLabel.text = "话题推荐"
    headerView.leftBackAction = { [weak self] in
      self?.leftBackAction?()
    }
    headerView.closeAction = { [weak self] in
      self?.closeAction?()
    }
    headerView.homeAction = { [weak self] in
      self?.homeAction?()
    }

    headerView.snp.makeConstraints { make in
      make.left.right.top.equalTo(self)
      make.height.equalTo(50)
    }

    addSubview(descriptionLabel)
    descriptionLabel.snp.makeConstraints { make in
      make.left.equalTo(self).offset(17)
      make.right.equalTo(self).offset(-17)
      make.top.equalTo(headerView.snp.bottom)
    }

    addSubview(mainCollectionView)
    mainCollectionView.snp.makeConstraints { make in
      make.left.equalTo(self).offset(16)
      make.right.equalTo(self).offset(-16)
      make.top.equalTo(descriptionLabel.snp.bottom).offset(16)
      make.bottom.equalTo(self)
    }
  }

  // MARK: lazyLoad

  lazy var headerView: NEOneOnOneAssistantHeaderView = {
    let header = NEOneOnOneAssistantHeaderView()
    header.backgroundColor = UIColor(hexString: "eef5fe")
    return header
  }()

  lazy var descriptionLabel: UILabel = {
    let descriptionLabel = UILabel()
    descriptionLabel.font = UIFont(name: "PingFangSC-Regular", size: 12)
    descriptionLabel.textColor = UIColor(hexString: "#337EFF")
    descriptionLabel.textAlignment = .left
    descriptionLabel.text = ne_localized("以下是给您推荐的聊天话题，可查看不同分类")
    descriptionLabel.numberOfLines = 0
    return descriptionLabel
  }()

  lazy var mainCollectionView: UICollectionView = {
    let layout = UICollectionViewFlowLayout()
    layout.itemSize = CGSize(width: (UIScreen.main.bounds.size.width - 32 - 20) / 3, height: 50)
    layout.scrollDirection = .vertical
    layout.minimumLineSpacing = 8
    layout.minimumInteritemSpacing = 8

    let mainCollectionView = UICollectionView(frame: CGRectZero, collectionViewLayout: layout)
    mainCollectionView.register(NEOneOnOneAssistantCell.self, forCellWithReuseIdentifier: NEOneOnOneAssistantCell.description())
    mainCollectionView.delegate = self
    mainCollectionView.dataSource = self
    mainCollectionView.showsHorizontalScrollIndicator = false
    mainCollectionView.allowsMultipleSelection = false
    mainCollectionView.backgroundColor = UIColor(hexString: "eef5fe")
    return mainCollectionView
  }()

  // MARK: UICollectionViewDataSource

  func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    sourceData.count
  }

  func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
    if let cell = collectionView.dequeueReusableCell(withReuseIdentifier: NEOneOnOneAssistantCell.description(), for: indexPath) as? NEOneOnOneAssistantCell
    {
      let itemData = sourceData[indexPath.row] as NEOneOnOneAIGCTopicExtention
      cell.itemButton.setTitle(itemData.topic?.desc, for: .normal)
      cell.itemButton.layer.borderColor = UIColor(hexString: itemData.boardColor!).cgColor
      cell.itemButton.backgroundColor = UIColor(hexString: itemData.backGroundColor!)
      cell.itemButton.setTitleColor(UIColor(hexString: itemData.textColor!), for: .normal)
      cell.itemClickAction = { [weak self] in

        guard let self = self else { return }
        self.itemClickAction?(indexPath.row, itemData.topic)
        print("点击话题:\(String(describing: itemData.topic?.desc?.description))")
      }
      return cell
    }

    return UICollectionViewCell()
  }
}

class NEOneOnOneAssistantCell: UICollectionViewCell {
  var itemClickAction: (() -> Void)?
  override init(frame: CGRect) {
    super.init(frame: frame)
    loadSubviews()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  func loadSubviews() {
    contentView.backgroundColor = UIColor(hexString: "eef5fe")
    contentView.addSubview(itemButton)
    itemButton.snp.makeConstraints { make in
      make.top.left.right.bottom.equalTo(self.contentView)
    }
  }

  lazy var itemButton: UIButton = {
    let itemButton = UIButton()
    // 设置边框宽度和颜色
    itemButton.layer.borderWidth = 1
    itemButton.layer.borderColor = UIColor.red.cgColor

    // 设置圆角
    itemButton.layer.cornerRadius = 8
    itemButton.clipsToBounds = true // 必须设置这个属性为 true 才能生效
    itemButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    itemButton.addTarget(self, action: #selector(clickItemButton), for: .touchUpInside)
    return itemButton
  }()

  // MARK: actionSelector

  @objc func clickItemButton() {
    itemClickAction?()
  }
}

class NEOneOnOneAIGCTopicExtention: NSObject, Codable {
  var topic: NEOneOnOneAIGCTopic?
  var boardColor: String?
  var backGroundColor: String?
  var textColor: String?
}
