// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NEOneOnOneKit
import SnapKit
import UIKit

class NEOneOnOneAIViewController: UIViewController {
  var lastMessage: String?
  //
  var copyCompletion: ((String) -> Void)?

  override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
    super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    modalPresentationStyle = .overFullScreen
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override func viewDidLoad() {
    super.viewDidLoad()
    let background = UIView(frame: view.bounds)
    background.backgroundColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.7)
    view.addSubview(background)
    let gesture = UITapGestureRecognizer(target: self, action: #selector(dismissAIViewController))
    background.addGestureRecognizer(gesture)

    view.addSubview(homeView)
    homeView.isHidden = false
    homeView.snp.makeConstraints { make in
      make.left.right.bottom.equalToSuperview()
      make.height.equalTo(283)
    }

    view.addSubview(contentView)
    contentView.isHidden = true
    contentView.snp.makeConstraints { make in
      make.left.right.bottom.equalToSuperview()
      make.height.equalTo(490)
    }

    view.addSubview(topicView)
    topicView.isHidden = true
    topicView.snp.makeConstraints { make in
      make.left.right.bottom.equalToSuperview()
      make.height.equalTo(283)
    }

    view.addSubview(chatSkillsView)
    chatSkillsView.isHidden = true
    chatSkillsView.snp.makeConstraints { make in
      make.left.right.bottom.equalToSuperview()
      make.height.equalTo(490)
    }

    view.addSubview(replayEmptyView)
    replayEmptyView.isHidden = true
    replayEmptyView.snp.makeConstraints { make in
      make.left.right.bottom.equalToSuperview()
      make.height.equalTo(490)
    }
  }

  @objc func dismissAIViewController() {
    presentedViewController?.dismiss(animated: false)
    dismiss(animated: true)
  }

  lazy var homeView: NEOneOnOneAIHomeView = {
    let view = NEOneOnOneAIHomeView(frame: view.bounds)
    view.delegate = self
    return view
  }()

  lazy var contentView: NEOneOnOneAssistantView = {
    let view = NEOneOnOneAssistantView(frame: view.bounds)
    view.homeAction = { [weak self] in
      self?.showHomeView()
    }
    view.closeAction = { [weak self] in
      self?.dismissAIViewController()
    }
    view.leftBackAction = { [weak self] in
      self?.showHomeView()
    }
    view.contentView.addSubview(tableView)
    tableView.snp.makeConstraints { make in
      make.top.left.bottom.right.equalToSuperview()
    }
    view.contentView.addSubview(requestFailedView)
    requestFailedView.snp.makeConstraints { make in
      make.top.left.bottom.right.equalToSuperview()
    }
    return view
  }()

  lazy var chatSkillsView: NEOneOnOneAssistantView = {
    let view = NEOneOnOneAssistantView(frame: view.bounds)
    view.homeAction = { [weak self] in
      self?.showHomeView()
    }
    view.closeAction = { [weak self] in
      self?.dismissAIViewController()
    }
    view.leftBackAction = { [weak self] in
      self?.showSkill(shouldRefresh: false)
    }
    view.contentView.addSubview(textView)
    textView.snp.makeConstraints { make in
      make.top.bottom.equalToSuperview()
      make.left.equalToSuperview().offset(24)
      make.right.equalToSuperview().offset(-24)
    }
    view.refreshButton.isHidden = true
    view.headerView.headerTitleLabel.text = ne_localized("聊天技巧")
    return view
  }()

  lazy var topicView: NEOneOnOneAssistantTopicView = {
    let view = NEOneOnOneAssistantTopicView(frame: view.bounds)
    view.leftBackAction = { [weak self] in
      self?.showHomeView()
    }
    view.homeAction = { [weak self] in
      self?.showHomeView()
    }
    view.closeAction = { [weak self] in
      self?.dismissAIViewController()
    }
    view.itemClickAction = { [weak self] _, topic in
      self?.contentView.leftBackAction = {
        self?.showTopicView()
      }
      self?.contentView.headerView.headerTitleLabel.text = topic?.desc
      self?.contentView.descriptionLabel.text = ne_localized("以下是给您推荐的开场话术，可直接复制到输入框")
      self?.contentView.refreshAction = {
        if let topicType = topic?.topicType {
          self?.contentView.showLoadingView()
          NEOneOnOneKit.getInstance().topicRecommend(topicType: topicType) { code, msg, list in
            if let list = list {
              self?.dataSource = list
              DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                self?.contentView.hideLoadingView()
                self?.tableView.isHidden = false
                self?.requestFailedView.isHidden = true
                self?.tableView.reloadData()
              }
            } else {
              DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                self?.contentView.hideLoadingView()
                self?.tableView.isHidden = true
                self?.requestFailedView.isHidden = false
              }
            }
          }
        }
      }
      self?.cellType = .copy
      self?.showContentView(shouldRefresh: true)
    }
    return view
  }()

  lazy var replayEmptyView: NEOneOnOneAssistantView = {
    let view = NEOneOnOneAssistantView(frame: view.bounds)
    view.homeAction = { [weak self] in
      self?.showHomeView()
    }
    view.closeAction = { [weak self] in
      self?.dismissAIViewController()
    }
    view.leftBackAction = { [weak self] in
      self?.showHomeView()
    }
    let imageView = UIImageView(image: ne_chatUI_imageName(imageName: "ai_replay_empty"))
    view.contentView.addSubview(imageView)
    imageView.snp.makeConstraints { make in
      make.width.height.equalTo(112)
      make.center.equalToSuperview()
    }
    let label = UILabel()
    label.text = ne_localized("对方的信息不包含任何中文字符\n无法给出回复建议")
    label.textColor = UIColor(red: 0.702, green: 0.716, blue: 0.737, alpha: 1)
    label.font = UIFont(name: "PingFangSC-Regular", size: 14)
    label.numberOfLines = 0
    label.lineBreakMode = .byWordWrapping
    label.textAlignment = .center
    view.contentView.addSubview(label)
    label.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.top.equalTo(imageView.snp.bottom).offset(18)
    }
    view.refreshButton.isHidden = true
    view.headerView.headerTitleLabel.text = ne_localized("对话回复")
    view.descriptionLabel.isHidden = true
    return view
  }()

  lazy var requestFailedView: UIView = {
    let view = UIView(frame: view.bounds)
    let imageView = UIImageView(image: ne_chatUI_imageName(imageName: "ai_replay_empty"))
    view.addSubview(imageView)
    imageView.snp.makeConstraints { make in
      make.width.height.equalTo(112)
      make.center.equalToSuperview()
    }
    let label = UILabel()
    label.text = ne_localized("暂无结果，点击下面的“换一批”重新试试吧")
    label.textColor = UIColor(red: 0.702, green: 0.716, blue: 0.737, alpha: 1)
    label.font = UIFont(name: "PingFangSC-Regular", size: 14)
    label.numberOfLines = 0
    label.lineBreakMode = .byWordWrapping
    label.textAlignment = .center
    view.addSubview(label)
    label.snp.makeConstraints { make in
      make.left.right.equalToSuperview()
      make.top.equalTo(imageView.snp.bottom).offset(18)
    }
    view.isHidden = true
    return view
  }()

  var dataSource = [String]()
  lazy var tableView: UITableView = {
    let view = UITableView(frame: view.bounds, style: .plain)
    view.dataSource = self
    view.delegate = self
    view.register(NEOneOnOneAICell.self, forCellReuseIdentifier: NEOneOnOneAICell.description())
    view.allowsSelection = false
    view.backgroundColor = UIColor(hexString: "eef5fe")
    return view
  }()

  var cellType = NEOneOnOneAICellAccessoryType.copy

  lazy var textView: UITextView = {
    let view = UITextView()
    view.textColor = UIColor(red: 0.2, green: 0.2, blue: 0.2, alpha: 1)
    view.font = UIFont(name: "PingFangSC-Regular", size: 14)
    view.backgroundColor = UIColor(hexString: "eef5fe")
    return view
  }()

  func showHomeView() {
    homeView.isHidden = false
    contentView.isHidden = true
    topicView.isHidden = true
    chatSkillsView.isHidden = true
    replayEmptyView.isHidden = true
  }

  func showContentView(shouldRefresh: Bool = false) {
    homeView.isHidden = true
    contentView.isHidden = false
    topicView.isHidden = true
    chatSkillsView.isHidden = true
    replayEmptyView.isHidden = true

    if shouldRefresh {
      dataSource.removeAll()
      tableView.reloadData()
      tableView.isHidden = false
      requestFailedView.isHidden = true
      contentView.refresh()
    }
  }

  func showTopicView() {
    homeView.isHidden = true
    contentView.isHidden = true
    topicView.isHidden = false
    chatSkillsView.isHidden = true
    replayEmptyView.isHidden = true
  }

  func showChatSkillsView(context: String) {
    homeView.isHidden = true
    contentView.isHidden = true
    topicView.isHidden = true
    chatSkillsView.isHidden = false
    replayEmptyView.isHidden = true

    chatSkillsView.descriptionLabel.text = context
    chatSkillsView.showLoadingView()
    NEOneOnOneKit.getInstance().chatSkillsWithContext(context) { [weak self] code, msg, content in
      DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
        self?.chatSkillsView.hideLoadingView()
        self?.textView.text = content
      }
    }
  }

  func showReplayEmptyView() {
    homeView.isHidden = true
    contentView.isHidden = true
    topicView.isHidden = true
    chatSkillsView.isHidden = true
    replayEmptyView.isHidden = false
  }
}

extension NEOneOnOneAIViewController: NEOneOnOneAIHomeDelegate {
  // 开场话术
  func homeBegin() {
    contentView.headerView.headerTitleLabel.text = ne_localized("开场话术")
    contentView.descriptionLabel.text = ne_localized("以下是给您推荐的开场话术，可直接复制到输入框")
    contentView.leftBackAction = { [weak self] in
      self?.showHomeView()
    }
    contentView.refreshAction = { [weak self] in
      self?.contentView.showLoadingView()
      NEOneOnOneKit.getInstance().getOpeningRemark { code, msg, list in
        if let list = list {
          self?.dataSource = list
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            self?.contentView.hideLoadingView()
            self?.tableView.isHidden = false
            self?.requestFailedView.isHidden = true
            self?.tableView.reloadData()
          }
        } else {
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            self?.contentView.hideLoadingView()
            self?.tableView.isHidden = true
            self?.requestFailedView.isHidden = false
          }
        }
      }
    }
    cellType = .copy
    showContentView(shouldRefresh: true)
  }

  // 话题推荐
  func homeTopic() {
    showTopicView()
  }

  // 聊天技巧
  func homeSkill() {
    showSkill()
  }

  private func showSkill(shouldRefresh: Bool = true) {
    contentView.headerView.headerTitleLabel.text = ne_localized("聊天技巧")
    contentView.descriptionLabel.text = ne_localized("以下是给您推荐的交友聊天技巧")
    contentView.leftBackAction = { [weak self] in
      self?.showHomeView()
    }
    contentView.refreshAction = { [weak self] in
      self?.contentView.showLoadingView()
      NEOneOnOneKit.getInstance().chatSkills { code, msg, list in
        if let list = list {
          self?.dataSource = list
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            self?.contentView.hideLoadingView()
            self?.tableView.isHidden = false
            self?.requestFailedView.isHidden = true
            self?.tableView.reloadData()
          }
        } else {
          DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            self?.contentView.hideLoadingView()
            self?.tableView.isHidden = true
            self?.requestFailedView.isHidden = false
          }
        }
      }
    }
    cellType = .detail
    showContentView(shouldRefresh: shouldRefresh)
  }

  // 对话回复
  func homeReplay() {
    contentView.headerView.headerTitleLabel.text = ne_localized("对话回复")
    contentView.leftBackAction = { [weak self] in
      self?.showHomeView()
    }
    if let lastMessage = lastMessage {
      contentView.descriptionLabel.text = ne_localized("对方在问：“\(lastMessage)”，以下是回复推荐")
      contentView.refreshAction = { [weak self] in
        self?.contentView.showLoadingView()
        NEOneOnOneKit.getInstance().chatReply(msg: lastMessage) { code, msg, list in
          self?.contentView.hideLoadingView()
          if let list = list {
            self?.dataSource = list
            DispatchQueue.main.async {
              self?.tableView.isHidden = false
              self?.requestFailedView.isHidden = true
              self?.tableView.reloadData()
            }
          } else {
            DispatchQueue.main.async {
              self?.tableView.isHidden = true
              self?.requestFailedView.isHidden = false
            }
          }
        }
      }
      cellType = .copy
      showContentView(shouldRefresh: true)
    } else {
      showReplayEmptyView()
    }
  }
}

extension NEOneOnOneAIViewController: UITableViewDataSource {
  func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    if let cell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneAICell.description()) as? NEOneOnOneAICell {
      cell.delegate = self
      cell.content = dataSource[indexPath.row]
      cell.type = cellType
      return cell
    }
    return UITableViewCell()
  }

  func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    dataSource.count
  }
}

extension NEOneOnOneAIViewController: UITableViewDelegate {
  func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    NEOneOnOneAICell.calculateCellHeight(dataSource[indexPath.row])
  }
}

extension NEOneOnOneAIViewController: NEOneOnOneAICellDelegaet {
  func accessoryButtonCopy(content: String?) {
    if let content = content {
      showToast(ne_localized("复制成功"))
      copyCompletion?(content)
    }
  }

  func accessoryButtonDetail(content: String?) {
    if let content = content {
      showChatSkillsView(context: content)
    }
  }
}
