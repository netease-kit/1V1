// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NIMSDK
import SnapKit
import UIKit

@objcMembers public class NEOneOnOneUserDetailViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
  public typealias PrivateLatterBlock = (String) -> Void
  /// 此处数据为呼叫需要时候，后续需要确认是否从接口中获取
  public var sessionId: String = ""
  public var isVirtualRoom: Bool = false
  public var nickName: String = ""
  public var icon: String = ""
  public var audioUrl: String?
  public var videoUrl: String?
  public var privateLatter: PrivateLatterBlock?

  // 点击事件
  public var reportAction: (() -> Void)?
  public var backAction: (() -> Void)?

  // 举报列表
  let reportArray = [ne_oneOnOne_localized("政治造谣"), ne_oneOnOne_localized("色情低俗"), ne_oneOnOne_localized("广告营销"), ne_oneOnOne_localized("欺诈信息")]

  override public func viewWillAppear(_ animated: Bool) {
    super.viewWillAppear(animated)
//           navigationController?.setNavigationBarHidden(true, animated: false)
    navigationController?.navigationBar.isHidden = true
  }

  override public func viewDidLoad() {
    super.viewDidLoad()

    addViews()
  }

  private func addViews() {
    view.addSubview(mainTableView)
    mainTableView.snp.makeConstraints { make in
      make.top.left.right.equalTo(self.view)
      make.bottom.equalTo(self.view).offset(-94)
    }

    view.addSubview(backButton)
    backButton.snp.makeConstraints { make in
      make.top.equalTo(self.view).offset(57)
      make.left.equalTo(self.view).offset(20)
      make.width.height.equalTo(26)
    }

    view.addSubview(settingButton)
    settingButton.snp.makeConstraints { make in
      make.right.equalTo(self.view).offset(-20)
      make.width.height.equalTo(26)
      make.centerY.equalTo(backButton)
    }

    view.addSubview(bottomPresentView)
  }

  // MARK: lazy Load

  // 举报

  lazy var settingButton: UIButton = {
    let settingButton = UIButton()
    settingButton.backgroundColor = .red
    settingButton.setImage(ne_oneOnOne_imageName(imageName: "threePoint"), for: .normal)
    //        settingButton.setBackgroundImage(ne_chatUI_imageName(imageName: "header_back_icon"), for: .normal)
    settingButton.addTarget(self, action: #selector(clickSettingButton), for: .touchUpInside)
    return settingButton
  }()

  func clickSettingButton() {
    view.endEditing(true)
    let popView = NEOneOnOnePopView(frame: UIScreen.main.bounds)
    popView.isInBlack = NIMSDK.shared().userManager.isUser(inBlackList: sessionId)
    popView.sourceRect = CGRect(x: settingButton.frame.origin.x + settingButton.frame.width / 2, y: settingButton.frame.origin.y + settingButton.frame.height, width: settingButton.frame.size.width, height: settingButton.frame.size.height)
    view.addSubview(popView)
    popView.reportAction = { [weak self] in
      guard let self = self else { return }
      DispatchQueue.main.async { [weak self] in
        guard let self = self else { return }
        self.view.addSubview(self.reportView)
      }
      self.reportView.showView(callback: {})
      self.reportView.selectReportCallback = { [weak self] index in
        guard let self = self else { return }
        if index == self.reportArray.count {
          return
        } else {
          let report = self.reportArray[index]
          NEOneOnOneReportUtil.report(report, sessionId: self.sessionId)
        }
      }
    }
    popView.blockAction = {
      if NIMSDK.shared().userManager.isUser(inBlackList: self.sessionId) {
        NIMSDK.shared().userManager.remove(fromBlackBlackList: self.sessionId) { error in
          if let _ = error {
            NEOneOnOneToast.show(ne_oneOnOne_localized("取消拉黑失败"))
          } else {
            NEOneOnOneToast.show(ne_oneOnOne_localized("取消拉黑成功"))
          }
        }
      } else {
        NIMSDK.shared().userManager.add(toBlackList: self.sessionId) { error in
          if let _ = error {
            NEOneOnOneToast.show(ne_oneOnOne_localized("拉黑失败"))
          } else {
            NEOneOnOneToast.show(ne_oneOnOne_localized("拉黑成功"))
          }
        }
      }
    }
  }

  public lazy var mainTableView: UITableView = {
    var tableView = UITableView()
    tableView.register(NEOneOnOneUserDetailHeaderCell.self, forCellReuseIdentifier: NEOneOnOneUserDetailHeaderCell.description())

    tableView.register(NEOneOnOneUserDetailBodyCell.self, forCellReuseIdentifier: NEOneOnOneUserDetailBodyCell.description())
    tableView.register(NEOneOnOneUserDetailBottomCell.self, forCellReuseIdentifier: NEOneOnOneUserDetailBottomCell.description())

    tableView.delegate = self
    tableView.dataSource = self
    tableView.backgroundColor = UIColor.red
    tableView.separatorStyle = .none
    let statusBarHeight = UIApplication.shared.statusBarFrame.height
    if #available(iOS 11.0, *) {
      tableView.contentInsetAdjustmentBehavior = .never
    } else {
      automaticallyAdjustsScrollViewInsets = false
      tableView.contentInset = UIEdgeInsets(top: statusBarHeight, left: 0, bottom: 0, right: 0)
    }
    return tableView
  }()

  lazy var backButton: UIButton = {
    let backButton = UIButton()
    backButton.setImage(ne_oneOnOne_imageName(imageName: "white_back_icon"), for: .normal)
    backButton.addTarget(self, action: #selector(backButtonAction), for: .touchUpInside)
    return backButton
  }()

  lazy var reportButton: UIButton = {
    let reportButton = UIButton()
    reportButton.setTitle(ne_oneOnOne_localized("举报"), for: UIControl.State.normal)
    reportButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    reportButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    reportButton.addTarget(self, action: #selector(clickReportButton), for: .touchUpInside)
    return reportButton
  }()

  lazy var reachability: NEOneOnOneReachability = {
    let reach = NEOneOnOneReachability.forInternetConnection()
    return reach!
  }()

  // 举报
  lazy var reportView: NEOneOnOneReportView = {
    let reportView = NEOneOnOneReportView(frame: CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: UIScreen.main.bounds.size.height))
    reportView.reportArray = reportArray
    return reportView
  }()

  lazy var bottomPresentView: NEOneOnOneBottomConnectView = {
    let bottomConnectView = NEOneOnOneBottomConnectView(frame: CGRect(x: 0, y: self.view.bounds.height - 94, width: self.view.bounds.size.width, height: 94))
    bottomConnectView.clickAudioAction = { [weak self] in
      guard let self = self else {
        return
      }
      self.dealWithAction(true)
    }
    bottomConnectView.clickVideoAction = { [weak self] in
      guard let self = self else {
        return
      }
      self.dealWithAction(false)
    }

    bottomConnectView.clickChatUpAction = { [weak self] in
      guard let self = self else { return }
      let status: NetworkStatus = self.reachability.currentReachabilityStatus()
      if status == .NotReachable {
        NEOneOnOneToast.show(ne_oneOnOne_localized("网络异常，请稍后重试"))
        return
      }
      let session = NIMSession(self.sessionId, type: .P2P)
      let message = NIMMessage()
      let setting = NIMMessageSetting()
      setting.teamReceiptEnabled = true
      message.setting = setting
      message.text = ne_oneOnOne_localized("真心交友，愿意聊聊吗？\n很喜欢你呢~")

      do {
        try NIMSDK.shared().chatManager.send(message, to: session)
        NEOneOnOneToast.show(ne_oneOnOne_localized("搭讪成功"))
      } catch {
        return
      }
    }

    bottomConnectView.clickPrivateLatterAction = { [weak self] in
      // 私信
      guard let self = self else { return }
      self.privateLatter?(self.sessionId)
    }

    return bottomConnectView
  }()

  func clickReportButton() {
    if let reportAction = reportAction {
      reportAction()
    }
  }

  func backButtonAction() {
    navigationController?.popViewController(animated: true)

    if let backAction = backAction {
      backAction()
    }
  }

  // 具体处理

  func dealWithAction(_ isAudio: Bool) {
    NEOneOnOneUIKitCallEngine.getInstance.dealWithAction(self, sessionId: sessionId, isAudio: isAudio, isVirtualRoom: isVirtualRoom, nickName: nickName, icon: icon, audioUrl: audioUrl, videoUrl: videoUrl)
  }

  // MARK: tableView dataSource

  public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    4
  }

  public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    switch indexPath.row {
    case 0:
      if let cell: NEOneOnOneUserDetailHeaderCell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneUserDetailHeaderCell.description(), for: indexPath) as? NEOneOnOneUserDetailHeaderCell {
        return cell
      } else {
        return NEOneOnOneUserDetailHeaderCell()
      }
    case 1:
      if let cell: NEOneOnOneUserDetailBodyCell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneUserDetailBodyCell.description(), for: indexPath) as? NEOneOnOneUserDetailBodyCell {
        return cell
      } else {
        return NEOneOnOneUserDetailBodyCell()
      }
    case 2:
      if let cell: NEOneOnOneUserDetailBodyCell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneUserDetailBodyCell.description(), for: indexPath) as? NEOneOnOneUserDetailBodyCell {
        cell.maxVisiableCount = 5
        cell.title = "礼物"
        return cell
      } else {
        return NEOneOnOneUserDetailBodyCell()
      }
    case 3:
      if let cell: NEOneOnOneUserDetailBottomCell = tableView.dequeueReusableCell(withIdentifier: NEOneOnOneUserDetailBottomCell.description(), for: indexPath) as? NEOneOnOneUserDetailBottomCell {
        return cell
      } else {
        return NEOneOnOneUserDetailBottomCell()
      }

    default:
      return UITableViewCell()
    }
  }

  public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
    switch indexPath.row {
    case 0:
      return UIScreen.main.bounds.size.width + 107
    case 1:
      return 162
    case 2:
      return 162
    case 3:
      return 250
    default:
      return 0
    }
  }
}
