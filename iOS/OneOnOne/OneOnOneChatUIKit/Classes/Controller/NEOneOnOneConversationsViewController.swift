// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import NEConversationUIKit
import UIKit

public class NEOneOnOneConversationsViewController: ConversationController, NIMConversationManagerDelegate {
  public init() {
    super.init(nibName: nil, bundle: nil)
    NIMSDK.shared().conversationManager.add(self)
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  override public func viewDidLoad() {
    listCtrl = NEOneOnOneConversationsListViewController()

    super.viewDidLoad()
    navView.brandBtn.setTitle(ne_localized("消息"), for: .normal)
    navView.brandBtn.setImage(nil, for: .normal)
    navView.brandBtn.layoutButtonImage(style: .left, space: 0)
    navView.searchBtn.isHidden = true
    navView.addBtn.setImage(ne_chatUI_imageName(imageName: "unread_clear"), for: .normal)
    navigationController?.tabBarItem.accessibilityIdentifier = "id.tvUnread"
  }

  override public func didClickAddBtn() {
    let alert = NEOneOnOneAlertViewController(
      title: ne_localized("是否忽略所有未读提醒？"),
      content: ne_localized("消息的未读红点会清除，忽略未读不会删除相关聊天记录"),
      leftAction: NEOneOnOneAlertAction(title: ne_localized("取消")),
      rightAction: NEOneOnOneAlertAction(title: ne_localized("确定"), handler: { [weak self] in
        // 清空未读消息
        self?.viewmodel.repo.clearAllUnreadCount()
      })
    )
    alert.show(viewController: self)
  }

  // 要在登出的时候重置状态，所以开放给Party调用
  public func refreshNotificationTips() {
    if let listCtrl = listCtrl as? NEOneOnOneConversationsListViewController {
      listCtrl.shouldCheckNotificationSettings = true
      listCtrl.checkNotificationSettings()
    }
  }

  public func getMsgUnreadCount() {
    let count = viewmodel.repo.getMsgUnreadCount(notify: true)
    updateUnreadCount(count)
  }

  func updateUnreadCount(_ totalUnreadCount: Int) {
    if totalUnreadCount > 0 {
      if totalUnreadCount > 99 {
        navigationController?.tabBarItem.badgeValue = "99+"
      } else {
        navigationController?.tabBarItem.badgeValue = String(totalUnreadCount)
      }
    } else {
      navigationController?.tabBarItem.badgeValue = nil
    }
  }

  public func didAdd(_ recentSession: NIMRecentSession, totalUnreadCount: Int) {
    updateUnreadCount(totalUnreadCount)
  }

  public func didRemove(_ recentSession: NIMRecentSession, totalUnreadCount: Int) {
    updateUnreadCount(totalUnreadCount)
  }

  public func didUpdate(_ recentSession: NIMRecentSession, totalUnreadCount: Int) {
    updateUnreadCount(totalUnreadCount)
  }

  public func allMessagesRead() {
    updateUnreadCount(0)
    listCtrl.reloadTableView()
  }
}

public class NEOneOnOneConversationsListViewController: ConversationListViewController, NEOneOnOnePushSettingViewDelegate {
  public var shouldCheckNotificationSettings = true

  override public func viewDidLoad() {
    topViewHeight = 40
    topView.addSubview(headerView)

    super.viewDidLoad()
    // 自定义cell
//    tableView.register(
//      NEOneOnOneConversionCell.self,
//      forCellReuseIdentifier: "\(NSStringFromClass(NEOneOnOneConversionCell.self))"
//    )

    NotificationCenter.default.addObserver(forName: UIApplication.willEnterForegroundNotification, object: nil, queue: nil) { noti in
      self.checkNotificationSettings()
    }

    emptyView.setEmptyImage(image: ne_chatUI_imageName(imageName: "conversion_empty"))
    emptyView.settingContent(content: ne_localized("当前没有会话"))
  }

  override public func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
    checkNotificationSettings()
  }

  func checkNotificationSettings() {
    guard shouldCheckNotificationSettings else {
      return
    }
    if #available(iOS 10.0, *) {
      UNUserNotificationCenter.current().getNotificationSettings { settings in
        switch settings.authorizationStatus {
        case .denied:
          // 用户已经拒绝推送通知，需要提醒用户打开通知权限。
          DispatchQueue.main.async {
            self.topViewHeight = 40
          }
        default:
          DispatchQueue.main.async {
            self.topViewHeight = 0
          }
        }
      }
    }
  }

  lazy var headerView: NEOneOnOnePushSettingView = {
    let headerView = NEOneOnOnePushSettingView(frame: CGRect(x: 0, y: 0, width: view.frame.width, height: 40))
    headerView.delegate = self
    return headerView
  }()

  public func deleteAction(action: UITableViewRowAction, indexPath: IndexPath) {
    let alert = NEOneOnOneAlertViewController(
      title: ne_localized("删除会话"),
      content: ne_localized("是否确认将该会话删除？"),
      leftAction: NEOneOnOneAlertAction(title: ne_localized("取消")),
      rightAction: NEOneOnOneAlertAction(title: ne_localized("确定"), handler: { [weak self] in
        self?.deleteActionHandler(action: action, indexPath: indexPath)
      })
    )
    alert.show(viewController: self)
  }

  public func topAction(action: UITableViewRowAction, indexPath: IndexPath, isTop: Bool) {
    if NEChatDetectNetworkTool.shareInstance.manager?.networkReachabilityStatus == .notReachable {
      showToast(ne_localized("网络异常，请稍后重试"))
      return
    }
    topActionHandler(action: action, indexPath: indexPath, isTop: isTop)
  }

//  public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
//    72
//  }

  override public func tableView(_ tableView: UITableView,
                                 editActionsForRowAt indexPath: IndexPath) -> [UITableViewRowAction]? {
    weak var weakSelf = self
    var rowActions = [UITableViewRowAction]()

    let conversationModel = weakSelf?.viewModel.conversationListArray?[indexPath.row]
    guard let recentSession = conversationModel?.recentSession,
          let session = recentSession.session else {
      return rowActions
    }

    let deleteAction = UITableViewRowAction(style: .destructive,
                                            title: ne_localized("删除")) { action, indexPath in
      weakSelf?.deleteAction(action: action, indexPath: indexPath)
    }

    // 置顶和取消置顶
    let isTop = viewModel.stickTopInfos[session] != nil
    let topAction = UITableViewRowAction(style: .destructive,
                                         title: isTop ? ne_localized("取消置顶") :
                                           ne_localized("置顶")) { action, indexPath in
      weakSelf?.topAction(action: action, indexPath: indexPath, isTop: isTop)
    }
    deleteAction.backgroundColor = NEConstant.hexRGB(0xE83A39)
    topAction.backgroundColor = NEConstant.hexRGB(0x2A6BF2)
    rowActions.append(deleteAction)
    rowActions.append(topAction)

    return rowActions
  }

  override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
//    let cell = tableView.dequeueReusableCell(
//      withIdentifier: "\(NSStringFromClass(NEOneOnOneConversionCell.self))",
//      for: indexPath
//    ) as! NEOneOnOneConversionCell
//    if let count = viewModel.conversationListArray?.count,
//       count > indexPath.row {
//      let conversationModel = viewModel.conversationListArray?[indexPath.row]
//      cell.topStickInfos = viewModel.stickTopInfos
//      cell.configData(sessionModel: conversationModel)
//    }
//    return cell
    let cell = super.tableView(tableView, cellForRowAt: indexPath)
    if let cell = cell as? ConversationListCell,
       let conversationModel = viewModel.conversationListArray?[indexPath.row],
       let lastMessage = conversationModel.recentSession?.lastMessage {
      let text = NEOneOnOneMessageUtil.messageContent(message: lastMessage)
      cell.subTitle.attributedText = text
    }
    return cell
  }

  func settingViewToStartPush(_ view: NEOneOnOnePushSettingView) {
    if let settingsURL = URL(string: UIApplication.openSettingsURLString) {
      if #available(iOS 10.0, *) {
        UIApplication.shared.open(settingsURL)
      }
    }
  }

  func settingViewClose(_ view: NEOneOnOnePushSettingView) {
    shouldCheckNotificationSettings = false
    topViewHeight = 0
  }
}
