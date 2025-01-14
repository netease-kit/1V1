// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import NEChatKit
import NEChatUIKit
import NEOneOnOneKit
import NEOneOnOneUIKit
import NEUIKit
import NIMSDK
import SnapKit
import UIKit

let tag = "NEOneOnOneChatP2PViewController"
public class NEOneOnOneChatP2PViewController: P2PChatViewController, NIMEventSubscribeManagerDelegate, UIPopoverPresentationControllerDelegate, NEOneOnOneGiftViewDelegate, NEOneOnOneListener {
  var thanksForGiven: ((String) -> Void)?
  var _audioInputingView: NEOneOnOneAudioInputingView?
  // 是否加入房间中,防止重复点击.
  var isEnterRoom: Bool = false

  // 对端用户是否输入中
  var remoteUserIsEditing = false
  // 对端用户是否在线
  var remoteUserIsOnline = false
  // 是否为音频通话
  var isAudio: Bool = true

  let hotTopicArray = [ne_localized("哈喽，在忙吗？"), ne_localized("感觉我们很合拍，能认识一下吗？"), ne_localized("我在这里等了一段时间，想和你聊聊天"), ne_localized("刚好看到你，感觉很有缘"), ne_localized("你的照片很好看，能聊聊吗？"), ne_localized("你成功引起了我的注意")]

  let reportArray = [ne_localized("政治造谣"), ne_localized("色情低俗"), ne_localized("广告营销"), ne_localized("欺诈信息")]

  var remoteUserInfo: NEOneOnOneAccountInfo?
  /// 顶部视图
  override public func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    navigationController?.setNavigationBarHidden(true, animated: true)
  }

  override public func viewDidLoad() {
    NotificationCenter.default.post(Notification(name: Notification.Name("messageEnter")))

    navigationItem.rightBarButtonItem?.customView?.backgroundColor = UIColor.green

    ChatUIConfig.shared.messageProperties.selfMessageBg = .clear
    ChatUIConfig.shared.messageProperties.rightBubbleBg = ne_chatUI_imageName(imageName: "right_back_icon")
    ChatUIConfig.shared.messageProperties.receiveMessageBg = .clear
    ChatUIConfig.shared.messageProperties.leftBubbleBg = ne_chatUI_imageName(imageName: "left_back_icon")
            
    if viewModel.conversationId != "yunxinassistaccid_1" {
      bodyBottomViewHeight = 95
      bodyBottomView.addSubview(quickReplayView)
      quickReplayView.snp.makeConstraints { make in
        make.top.left.right.equalTo(bodyBottomView)
        make.height.equalTo(85)
      }
      navigationView.titleBarBottomLine.backgroundColor = .clear
      view.bringSubviewToFront(quickReplayView)
      quickReplayView.selectQuickReply = { [weak self] index in
        if let self = self {
          print("selectQuickReply:\(index)")
          if index < self.quickReplayView.oneOnOneQuickReplyArray.count {
            self.viewModel.sendTextMessage(text: self.quickReplayView.oneOnOneQuickReplyArray[index]) { message, error in
            }
          }
        }
      }
      quickReplayView.rebortClicked = { [weak self] in
        if let conversationId = self?.viewModel.conversationId {
          let ai = NEOneOnOneAIViewController()
          ai.copyCompletion = { content in
            DispatchQueue.main.async {
              self?.chatInputView.textView.text = content
              self?.chatInputView.textView.becomeFirstResponder()
              ai.dismissAIViewController()
            }
          }

//          if let messages = NIMSDK.shared().conversationManager.messages(in: session, message: nil, limit: 30) {
//            let recvMessages = messages.filter { message in
//              (message.from == session.sessionId) && (message.messageType == .text)
//            }
//            if let lastMessage = recvMessages.last?.text {
//              ai.lastMessage = lastMessage
//            }
//          }
//

          let option = V2NIMMessageListOption()
          option.limit = 30
          option.conversationId = conversationId

          NIMSDK.shared().v2MessageService.getMessageList(option) { messages in
            let recvMessages = messages.filter { msg in
              (msg.isSelf == false) && (msg.messageType == .MESSAGE_TYPE_TEXT)
            }
            if let lastMessage = recvMessages.last?.text {
              ai.lastMessage = lastMessage
            }
            self?.present(ai, animated: true)
          } failure: { error in
          }
        }
      }
    }
    cellRegisterDic[String(OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TIPS_TYPE)] = NEOneOnOneTextSalutuionCell.self
    cellRegisterDic[String(OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE)] = NEOneOnOneTextThirdPrivacyCell.self
    cellRegisterDic[String(OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE)] = NEOneOnOneAudioSalutuionCell.self
    cellRegisterDic[String(OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE)] = NEOneOnOneVideoSalutuionCell.self
    cellRegisterDic[String(OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE)] = NEOneOnOneTextNonComplianceCell.self
    cellRegisterDic[String(SEND_GIFT_TYPE_SEND)] = NEOneOnOneRewardRightCell.self
    cellRegisterDic[String(SEND_GIFT_TYPE_RECV)] = NEOneOnOneRewardLeftCell.self
    cellRegisterDic[String(OneOnOneChatCustomMessageType.OFFICIAL_GIFT_TYPE)] = NEOneOnOneOfficialCell.self

    NEChatUIKitClient.instance.regsiterCustomCell([String(OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TIPS_TYPE): NEOneOnOneTextSalutuionCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE): NEOneOnOneTextThirdPrivacyCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE): NEOneOnOneAudioSalutuionCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE): NEOneOnOneVideoSalutuionCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE): NEOneOnOneTextNonComplianceCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(OneOnOneChatCustomMessageType.OFFICIAL_GIFT_TYPE): NEOneOnOneOfficialCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(SEND_GIFT_TYPE_SEND): NEOneOnOneRewardRightCell.self])
    NEChatUIKitClient.instance.regsiterCustomCell([String(SEND_GIFT_TYPE_RECV): NEOneOnOneRewardLeftCell.self])
      
    super.viewDidLoad()
    tableView.backgroundColor = UIColor.clear

    let imageView = UIImageView()
    imageView.image = ne_chatUI_imageName(imageName: "chat_back_icon")
    view.addSubview(imageView)
    imageView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(self.view)
    }
    view.insertSubview(imageView, belowSubview: navigationView)

    NotificationCenter.default.addObserver(self, selector: #selector(receiveInvite), name: NSNotification.Name("receiveInvite"), object: nil)

    NotificationCenter.default.addObserver(self, selector: #selector(startAudioCallAction), name: NSNotification.Name(AudioCallAction), object: nil)
    NotificationCenter.default.addObserver(self, selector: #selector(startVideoCallAction), name: NSNotification.Name(VideoCallAction), object: nil)

    // 聊天页输入框左右间距自定义
    chatInputView.textviewLeftConstraint?.constant = 72
    chatInputView.textviewRightConstraint?.constant = -72
    /// 设置内边距
    let inerInset = chatInputView.textView.textContainerInset
    chatInputView.textView.textContainerInset = UIEdgeInsets(top: inerInset.top, left: inerInset.left, bottom: inerInset.bottom, right: 40)

    // 自定义底部工具条(未点击更多状态)
    customBottomBar()

    /// 自定义聊天框周围视图
    customTextViewAroundView()
    // 更多按钮特殊处理
    chatInputView.stackView.addArrangedSubview(moreButton)

    if viewModel.conversationId != "yunxinassistaccid_1" {
      navigationView.isHidden = true
      /// 订阅对方在线状态
      NIMSDK.shared().subscribeManager.subscribeEvent(subscribeRequest) { error, failedPublishers in
        print("订阅是否成功 ---- \(String(describing: error))")
      }

      // 在线状态监听
      NIMSDK.shared().subscribeManager.add(self)
//      navigationItem.titleView = navigationItemView
      navigationItem.titleView?.contentMode = .center
      NEOneOnOneKit.getInstance().addOneOnOneListener(self)

      NotificationCenter.default.addObserver(forName: NSNotification.Name("SendGift"), object: nil, queue: nil) { [weak self] _ in
        self?.clickGiftButton()
      }
      addHeaderView()
      view.addSubview(giftAnimation)
    } else {
      /// 隐藏输入框
      navigationView.moreButton.isHidden = true
      navigationView.backgroundColor = .clear
      navigationItem.rightBarButtonItem = nil
      bodyBottomViewHeightAnchor?.constant = 0
      bottomViewTopAnchor?.constant = 0
    }
    updateOnlineState()

    /// 输入语音的时候，如果来呼叫了，页面隐藏
    NEOneOnOneUIKitCallEngine.getInstance.callComing = { [weak self] in
      /// 呼叫页面即将唤起
      if let _ = self?._audioInputingView {
        self?.audioInputingView.removeFromSuperview()
        self?.endRecord(insideView: false)
        self?._audioInputingView = nil
      }
    }
    
    // 反垃圾结果
    ChatKitClient.shared.sendMessageCallback = { [weak self] viewController, result, error, progress in
      if let antispamResult = result?.antispamResult {
        let jsonData = antispamResult.data(using: .utf8)!
        do {
          if let dictionary = try JSONSerialization.jsonObject(with: jsonData, options: []) as? [String: Any] {
            if dictionary.keys.contains("suggestion"), let suggestion = dictionary["suggestion"] as? Int {
              if suggestion != 0 {
                // 不通过
                if let extString = dictionary["ext"] as? String,
                   let extObjectData = extString.data(using: .utf8),
                   let extObject = try? JSONSerialization.jsonObject(with: extObjectData, options: []) as? [String: Any],
                   let antispam = extObject["antispam"] as? [String: Any],
                   let labels = antispam["labels"] as? [[String: Any]],
                   let subLabels = labels.first?["subLabels"] as? [[String: Any]],
                   let details = subLabels.first?["details"] as? [String: Any],
                   let keywords = details["keywords"] as? [[String: Any]],
                   let keyword = keywords.first?["word"] as? String {
                  if keyword.count > 0 {
                      self?.sendSalutionMsg(type: 3, antiSpamMessage: result?.message)
                    return
                  }
                }
                  self?.sendSalutionMsg(type: 4, antiSpamMessage: result?.message)
  
              } else {}
            }
          }
        } catch {
          print(error.localizedDescription)
        }
      }
    }
  }

  func updateOnlineState() {
    NEOneOnOneKit.getInstance().getAccountInfo(viewModel.conversationId) { [weak self] code, msg, accountInfo in
      if code != 0 {
        // 请求失败
      } else {
        guard let mobile = accountInfo?.mobile else {
          return
        }
        self?.remoteUserInfo = accountInfo
        NEOneOnOneKit.getInstance().getUserState(mobile, callback: { [weak self] code, msg, onlineState in
          if code == 0 {
            // 请求成功
            if onlineState != nil, onlineState == "online" {
              // 在线
              DispatchQueue.main.async {
                self?.headerView.navigationItemView.onlineLabel.isHidden = false
                self?.headerView.navigationItemView.onlineImageView.isHidden = false
              }
            }
          }
        })
      }
    }
  }

  override public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    let cell = super.tableView(tableView, cellForRowAt: indexPath)
    if let videoCell = cell as? ChatMessageVideoCell {
      videoCell.bubbleImageLeft.layer.cornerRadius = 8
      videoCell.bubbleImageLeft.clipsToBounds = true
      videoCell.bubbleImageRight.layer.cornerRadius = 8
      videoCell.bubbleImageRight.clipsToBounds = true
    } else if let imageCell = cell as? ChatMessageImageCell {
      imageCell.bubbleImageLeft.layer.cornerRadius = 8
      imageCell.bubbleImageLeft.clipsToBounds = true
      imageCell.bubbleImageRight.layer.cornerRadius = 8
      imageCell.bubbleImageRight.clipsToBounds = true
    }
    cell.backgroundColor = UIColor.clear
    cell.contentView.backgroundColor = UIColor.clear
    if cell.isKind(of: NEOneOnOneOfficialCell.self), let customCell = cell as? NEOneOnOneOfficialCell {
      customCell.thanksForGiven = { [weak self] data in
        // 数据解析
        if data.contains("party://chat/p2pChat"),
           let queryItems = URLComponents(url: URL(string: data)!, resolvingAgainstBaseURL: false)?.queryItems {
          for item in queryItems {
            if item.name == "user" {
              if let itemDetail = item.value,
                 itemDetail.count > 0 {
                let conversationId = V2NIMConversationIdUtil.p2pConversationId(itemDetail)

                Router.shared.use(
                  PushP2pChatVCRouter,
                  parameters: ["nav": self?.navigationController as Any, "conversationId": conversationId as Any],
                  closure: nil
                )
              }
              break
            }
          }
        }
      }
    }
    return cell
  }

  override public func didTapMessage(_ cell: UITableViewCell?, _ model: MessageContentModel?, _ replyIndex: Int? = nil) {
    if model?.type == .rtcCallRecord, let object = model?.message?.attachment as? V2NIMMessageCallAttachment {
      if object.type == 1 {
        dealWithAction(true)
        return
      } else {
        dealWithAction(false)
        return
      }
    }
    if model?.type == .audio || model?.type == .video,
       let canContinueActon = NEOneOnOneUIManager.sharedInstance().canContinueAction,
       !canContinueActon() {
      return
    }
    super.didTapMessage(cell, model, replyIndex)
  }

  override public func viewTap(tap: UITapGestureRecognizer) {
    if view.subviews.contains(operationView) {
      operationView.removeFromSuperview()
    } else {
      if chatInputView.textView.isFirstResponder {
        chatInputView.textView.resignFirstResponder()
      } else {
        if viewModel.conversationId != "yunxinassistaccid_1" {
          /// 云信小助手不展示键盘
          layoutInputView(offset: 0)
        }
      }
    }
  }

  override public func loadDataFinish() {
    super.loadDataFinish()
    if viewModel.conversationId == "yunxinassistaccid_1" {
      return
    }
    // 获取随机数
    let randomNum = Int(arc4random_uniform(2))
    // 获取本地存储数据
    let localKey = "\(NIMSDK.shared().loginManager.currentAccount())-\(viewModel.conversationId)"
    let hasSendSalution = UserDefaults.standard.bool(forKey: localKey)
    if !hasSendSalution {
      // 未发送
      sendSalutionMsg(type: 0, antiSpamMessage: nil)
      if randomNum == 0 {
        sendSalutionMsg(type: 1, antiSpamMessage: nil)
      } else {
        sendSalutionMsg(type: 2, antiSpamMessage: nil)
      }
      UserDefaults.standard.set(true, forKey: localKey)
    }
  }

  // MARK: - gift animation

  /// 播放礼物动画
  func playGiftWithName(name: String) {
    DispatchQueue.main.async {
      if UIApplication.shared.applicationState == .background {
        return
      }
      self.view.bringSubviewToFront(self.giftAnimation)
      self.giftAnimation.addGift(name)
    }
  }

  // MARK: lazyLoad

  // 动画视图
  lazy var giftAnimation: NEOneOnOneGiftView = {
    let giftAnimation = NEOneOnOneGiftView()
    giftAnimation.isHidden = true
    return giftAnimation
  }()

  // 顶部视图
  lazy var headerView: NEOneOnOneHeaderView = {
    let showTips = viewModel.conversationId.contains("virtually_user") && !NEOneOnOneKit.getInstance().isOversea
    var height = NEConstant.navigationHeight + 1
    if showTips {
      // tips高度36，间距14
      height += 50
    }
    bodyTopViewHeightAnchor?.constant = showTips ? 50 : 0
    let headerView = NEOneOnOneHeaderView(frame: CGRect(x: 0, y: NEConstant.statusBarHeight, width: view.bounds.width, height: height), showTips: showTips)
//    let headerView = NEOneOnOneHeaderView(frame: CGRect(x: 0, y: 0, width: self.view.bounds.self.width, height: 88))
    return headerView
  }()

  // 语音输入中视图
  var audioInputingView: NEOneOnOneAudioInputingView {
    if _audioInputingView == nil {
      _audioInputingView = NEOneOnOneAudioInputingView(frame: UIScreen.main.bounds)
      let audioInputingButton = NEOneOnOneSpeakButton(frame: CGRect(x: chatInputView.textView.frame.origin.x + chatInputView.frame.origin.x, y: chatInputView.textView.frame.origin.y + chatInputView.frame.origin.y, width: chatInputView.textView.frame.size.width, height: chatInputView.textView.frame.height))
      _audioInputingView?.addSubview(audioInputingButton)
    }
    _audioInputingView?.completeBlock = { [weak self] in
      self?.endRecord(insideView: true)
    }
    return _audioInputingView!
  }

  // 热聊话题
  lazy var hotTopicView: NEOneOnOneChatHotTopicsView = {
    let hotTopicView = NEOneOnOneChatHotTopicsView(frame: CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: UIScreen.main.bounds.size.height))
    hotTopicView.topicArray = hotTopicArray
    return hotTopicView
  }()

  // 举报
  lazy var reportView: NEOneOnOneReportView = {
    let reportView = NEOneOnOneReportView(frame: CGRect(x: 0, y: 0, width: self.view.frame.size.width, height: UIScreen.main.bounds.size.height))
    reportView.reportArray = reportArray
    return reportView
  }()

  // 订阅对象
  lazy var subscribeRequest: NIMSubscribeRequest = {
    let request = NIMSubscribeRequest()
    request.type = 1
    request.expiry = 60 * 60 * 24
    request.syncEnabled = true
    request.publishers = [viewModel.conversationId]
    return request
  }()

  // 更多按钮
  lazy var moreButton: UIButton = {
    let moreButton = UIButton()
    moreButton.setImage(ne_chatUI_imageName(imageName: "bottom_more_icon"), for: .normal)
    moreButton.setImage(ne_chatUI_imageName(imageName: "bottom_more_icon_select"), for: .selected)
    moreButton.tag = 4
    moreButton.isSelected = false
    moreButton.accessibilityIdentifier = "id.chatMessageActionItemBtn"
    moreButton.addTarget(self, action: #selector(buttonEvent), for: .touchUpInside)
    return moreButton
  }()

  // 语音切换视图
  lazy var audioExchangeButton: UIButton = {
    let audioExchangeButton = UIButton()
    audioExchangeButton.setImage(ne_chatUI_imageName(imageName: "audio_input_icon"), for: .normal)
    audioExchangeButton.setImage(ne_chatUI_imageName(imageName: "word_input_icon"), for: .selected)
    audioExchangeButton.isSelected = false
    audioExchangeButton.addTarget(self, action: #selector(clickAudioExchangeButton), for: .touchUpInside)
    return audioExchangeButton
  }()

  // 表情点击视图
  lazy var emojiButton: UIButton = {
    let emojiButton = UIButton()
    emojiButton.accessibilityIdentifier = "id.chatMessageInputEmojiIv"
    emojiButton.setImage(ne_chatUI_imageName(imageName: "emoji_icon"), for: .normal)
    emojiButton.setImage(ne_chatUI_imageName(imageName: "emoji_select_icon"), for: .selected)
    emojiButton.addTarget(self, action: #selector(clickEmojiButton), for: .touchUpInside)
    emojiButton.isSelected = false
    return emojiButton
  }()

  /// 礼物视图

  lazy var giftButton: UIButton = {
    let giftButton = UIButton()
    giftButton.setImage(ne_chatUI_imageName(imageName: "chat_gift_icon"), for: .normal)
    giftButton.setImage(ne_chatUI_imageName(imageName: "chat_gift_icon"), for: .selected)
    giftButton.addTarget(self, action: #selector(clickGiftButton), for: .touchUpInside)
    giftButton.accessibilityIdentifier = "id.chatMessageInputGiftBtn"
    return giftButton
  }()

  // 语音输入条
  lazy var audioInputButton: NEOneOnOneSpeakButton = {
    let audioInputButton = NEOneOnOneSpeakButton()
    let longPressGesture = UILongPressGestureRecognizer(target: self, action: #selector(longPressGestureAction(_:)))
    audioInputButton.addGestureRecognizer(longPressGesture)
    return audioInputButton
  }()

  /// 快速回复
  lazy var quickReplayView: NEOneOnOneQuickReply = {
    let quickReplayView = NEOneOnOneQuickReply()
    return quickReplayView
  }()

  // MARK: objc target func

  // 长按手势处理函数
  @objc func longPressGestureAction(_ gesture: UILongPressGestureRecognizer) {
    let semaphore = DispatchSemaphore(value: 0)

    var hasPermissions = false
    var firstRequest = false

    let microphoneStatus = AVCaptureDevice.authorizationStatus(for: .audio)

    switch microphoneStatus {
    case .authorized:
      hasPermissions = true
    case .denied: break
    case .notDetermined:
      firstRequest = true
    case .restricted: break
    @unknown default:
      fatalError("Unknown microphone status")
    }

    if firstRequest {
      AVCaptureDevice.requestAccess(for: .audio) { granted in
        if granted {
          hasPermissions = true
          semaphore.signal()
        } else {
          semaphore.signal()
        }
      }
    } else {
      semaphore.signal()
    }

    semaphore.wait()
    if !hasPermissions {
      NEOneOnOneToast.show(ne_localized("麦克风权限已关闭，请开启后重试"))
      return
    }
    if hasPermissions, firstRequest {
      /// 第一次申请不进行录制操作
      return
    }
    print("权限判断通过")

    // 判断是否在小窗：弹出Toast
    if let canContinueAction = NEOneOnOneUIManager.sharedInstance().canContinueAction,
       !canContinueAction() {
      /// 不能操作
      return
    }

    switch gesture.state {
    case .began:
      NEOneOnOneLog.infoLog(
        tag,
        desc: "开始录音"
      )
      // 添加全屏视图
      _audioInputingView = nil
      UIApplication.shared.keyWindow?.addSubview(audioInputingView)
      // TODO: 录制开始
      startRecord()
    case .changed:
      // 手指不离开进行滑动
      // 判断是否滑动到全屏视图的某一个区域
      let inreact = CGRectContainsPoint(audioInputingView.audioInputImageView.frame, gesture.location(in: UIApplication.shared.keyWindow))
      if inreact {
        audioInputingView.needCancel = true
      } else {
        audioInputingView.needCancel = false
      }
    case .ended:
      // 手指离开
      if !audioInputingView.needCancel {
        // 发送
        NEOneOnOneLog.infoLog(
          tag,
          desc: "结束录音并发送"
        )
        endRecord(insideView: true)
      } else {
        // 不发送
        // 发送
        NEOneOnOneLog.infoLog(
          tag,
          desc: "结束录音不发送"
        )
        endRecord(insideView: false)
      }
//      audioInputingView.endAudioiInputing()
      audioInputingView.removeFromSuperview()
    default:
      break
    }
  }

  @objc func clickGiftButton() {
    NEOneOnOneGiftViewController.show(viewController: self, delegate: self)
  }

  public func giftView(_ giftView: NEOneOnOneGiftViewController, sendGift gift: NEOneOnOneGiftItem, count: Int) {
    if NEChatDetectNetworkTool.shareInstance.manager?.networkReachabilityStatus == .notReachable {
      showToast(ne_localized("网络异常，请稍后重试"))
      return
    }
    // 展示礼物动画
    NEOneOnOneKit.getInstance().reward(giftId: gift.giftId, giftCount: count, target: viewModel.conversationId) { [weak self] code, send, obj in
      if code == 0 {
        self?.playGiftWithName(name: "anim_gift_0\(gift.giftId)")
      }
    }
  }

  @objc func clickAudioExchangeButton() {
    audioExchangeButton.isSelected = !audioExchangeButton.isSelected
    audioInputButton.isHidden = !audioExchangeButton.isSelected
//    moreButton.isSelected = false
    layoutInputView(offset: 0)
    emojiButton.isSelected = false
    if audioExchangeButton.isSelected {
      view.endEditing(true)
    }
    print("点击切换")
  }

  @objc func clickEmojiButton() {
    layoutInputView(offset: bottomExanpndHeight)
    chatInputView.addEmojiView()
//    layoutInputView(offset: 0)
    moreButton.isSelected = false
    emojiButton.isSelected = true
    print("点击表情")
  }

  // 文字切换语音 以及表情 礼物视图
  func customTextViewAroundView() {
    chatInputView.addSubview(audioExchangeButton)
    audioExchangeButton.snp.makeConstraints { make in
      make.right.equalTo(chatInputView.textView.snp.left).offset(-20)
      make.height.width.equalTo(32)
      make.centerY.equalTo(chatInputView.textView.snp.centerY)
    }

    chatInputView.addSubview(emojiButton)
    emojiButton.snp.makeConstraints { make in
      make.right.equalTo(chatInputView.textView.snp.right).offset(-12)
      make.height.width.equalTo(24)
      make.centerY.equalTo(chatInputView.textView.snp.centerY)
    }
    chatInputView.addSubview(giftButton)
    giftButton.snp.makeConstraints { make in
      make.left.equalTo(chatInputView.textView.snp.right).offset(20)
      make.height.width.equalTo(32)
      make.centerY.equalTo(chatInputView.textView.snp.centerY)
    }

    chatInputView.addSubview(audioInputButton)
    audioInputButton.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(chatInputView.textView)
    }
    view.bringSubviewToFront(audioInputButton)
    audioInputButton.isHidden = !audioExchangeButton.isSelected
  }

  func addHeaderView() {
    view.addSubview(headerView)
    headerView.backAction = { [weak self] in
      self?.navigationController?.popViewController(animated: true)
    }
    headerView.settingAction = { [weak self] in
      guard let self = self else { return }
      self.view.endEditing(true)
      let popView = NEOneOnOnePopView(frame: UIScreen.main.bounds)
      popView.isInBlack = NIMSDK.shared().userManager.isUser(inBlackList: self.viewModel.conversationId)
      popView.sourceRect = CGRect(x: self.headerView.frame.origin.x + self.headerView.settingButton.frame.origin.x, y: self.headerView.frame.origin.y + self.headerView.settingButton.frame.origin.y, width: self.headerView.settingButton.frame.size.width, height: self.headerView.settingButton.frame.size.height)
      self.view.addSubview(popView)
      popView.reportAction = { [weak self] in
        guard let self = self else { return }
        self.layoutInputView(offset: 0)
        self.emojiButton.isSelected = false
        self.moreButton.isSelected = false
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
            self.report(content: report)
          }
        }
      }
      popView.blockAction = { [weak self] in
        if let sessionId = self?.viewModel.conversationId {
          if NIMSDK.shared().userManager.isUser(inBlackList: sessionId) {
            NIMSDK.shared().userManager.remove(fromBlackBlackList: sessionId) { error in
              if let _ = error {
                NEOneOnOneToast.show(ne_localized("取消拉黑失败"))
              } else {
                NEOneOnOneToast.show(ne_localized("取消拉黑成功"))
              }
            }
          } else {
            NIMSDK.shared().userManager.add(toBlackList: sessionId) { error in
              if let _ = error {
                NEOneOnOneToast.show(ne_localized("拉黑失败"))
              } else {
                NEOneOnOneToast.show(ne_localized("拉黑成功"))
              }
            }
          }
        }
      }
    }
  }

  func report(content: String) {
    if let url = URL(string: "https://statistic.live.126.net/statics/report/common/form") {
      var version = "1.0.0"
      if let projectVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
        version = projectVersion
      }
      var request = URLRequest(url: url)
      request.httpMethod = "POST"
      request.addValue(version, forHTTPHeaderField: "ver")
      request.addValue(NIMSDK.shared().appKey() ?? "", forHTTPHeaderField: "appkey")
      request.addValue("allInOne", forHTTPHeaderField: "sdktype")
      let body = [
        "event": [
          "feedback": [
            "ver": version,
            "os_ver": UIDevice.current.systemVersion,
            "device_id": "device id",
            "description": content,
            "platform": "iOS",
            "manufacturer": "Apple",
            "app_key": NIMSDK.shared().appKey() ?? "",
            "phone": viewModel.conversationId,
            "nickname": viewModel.conversationId,
            "client": "allInOne",
            "model": "model",
            "time": Int(Date().timeIntervalSince1970 * 1000),
            "category": "举报",
          ],
        ],
      ]
      /// 请求session
      let sessionConfigure = URLSessionConfiguration.default
      sessionConfigure
        .httpAdditionalHeaders = ["Content-Type": "application/json;charset=utf-8"]
      sessionConfigure.timeoutIntervalForRequest = 10
      sessionConfigure.requestCachePolicy = .reloadIgnoringLocalCacheData
      let session = URLSession(configuration: sessionConfigure)
      if let data = try? JSONSerialization.data(withJSONObject: body, options: []) {
        request.httpBody = data
        let task = session.dataTask(with: request) { data, response, error in
          if let response = response as? HTTPURLResponse,
             response.statusCode == 200 {
            NEOneOnOneToast.show(ne_localized("举报成功"))
          } else {
            NEOneOnOneToast.show(ne_localized("举报失败"))
          }
        }
        task.resume()
      }
    }
  }

  // 自定义底部视图
  func customBottomBar() {
    let subviews = chatInputView.stackView.subviews
    for view in subviews {
      view.removeFromSuperview()
      chatInputView.stackView.removeArrangedSubview(view)
    }

    let normalImages = ["bottom_image_icon", "bottom_audio_icon", "bottom_camera_icon", "bottom_hot_topic_icon"]
    let selectImages = ["bottom_image_icon", "bottom_audio_icon", "bottom_camera_icon", "bottom_hot_topic_icon"]
    for i in 0 ..< normalImages.count {
      let bottomButton = UIButton()
      bottomButton.setImage(ne_chatUI_imageName(imageName: normalImages[i]), for: .normal)
      bottomButton.setImage(ne_chatUI_imageName(imageName: selectImages[i]), for: .selected)
      bottomButton.tag = i
      bottomButton.isSelected = false
      bottomButton.addTarget(self, action: #selector(buttonEvent), for: .touchUpInside)
      bottomButton.accessibilityIdentifier = "id.chatMessageActionItemBtn"
      chatInputView.stackView.addArrangedSubview(bottomButton)
    }
  }

  @objc func buttonEvent(_ btn: UIButton) {
    emojiButton.isSelected = false
    btn.isSelected = !btn.isSelected
    if btn.tag == 0 {
      /// 图片
      layoutInputView(offset: 0)
      moreButton.isSelected = false
      goPhotoAlbumWithVideo(self)
    } else if btn.tag == 1 {
      /// 语音
      layoutInputView(offset: 0)
      moreButton.isSelected = false
      dealWithAction(true)
    } else if btn.tag == 2 {
      /// 视频
      layoutInputView(offset: 0)
      moreButton.isSelected = false
      dealWithAction(false)
    } else if btn.tag == 3 {
      /// 热聊话题
      view.endEditing(true)
      layoutInputView(offset: 0)
      moreButton.isSelected = false

      DispatchQueue.main.async {
        self.view.addSubview(self.hotTopicView)
      }
      //            self.view.addSubview(view)
      hotTopicView.showView(callback: {})
      hotTopicView.selectTopicCallback = { [weak self] index in
        if let self = self {
          print("选择了第\(index)话题")
          if index < self.hotTopicArray.count {
            self.viewModel.sendTextMessage(text: self.hotTopicArray[index]) { message, error in
            }
          }
        }
      }
    } else if btn.tag == 4 {
      if !btn.isSelected {
        layoutInputView(offset: 0)
        moreButton.isSelected = false
      } else {
        if audioExchangeButton.isSelected {
          clickAudioExchangeButton()
        }
        layoutInputView(offset: bottomExanpndHeight)
        chatInputView.addMoreActionView()
        view.endEditing(true)
      }
    }
  }

  // 语音呼叫
  func startAudioCallAction() {
    NotificationCenter.default.post(Notification(name: Notification.Name("messageAudioCall")))
    stopPlay()
    dealWithAction(true)
  }

  /// 视频呼叫
  func startVideoCallAction() {
    NotificationCenter.default.post(Notification(name: Notification.Name("messageVideoCall")))
    stopPlay()
    dealWithAction(false)
  }

  func receiveInvite() {
    stopPlay()
  }

  // 发送打招呼消息：本地消息
  func sendSalutionMsg(type: Int, antiSpamMessage: V2NIMMessage?) {
//    let message = V2NIMMessage()
    let attachment = CustomAttachment(customJsonSrting: "")
    // 本地消息不回走到 CustomAttachment的解析，所以需要手动 cellHeight 以及 customType ，否则不会刷新。发送消息不需要
    attachment.cellHeight = 50 + 20
    switch type {
    case 1:
      // audio
      attachment.type = OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE
      attachment.customType = OneOnOneChatCustomMessageType.TRY_AUDIO_CALL_MESSAGE_TYPE
    case 2:
      // video
      attachment.type = OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE
      attachment.customType = OneOnOneChatCustomMessageType.TRY_VIDEO_CALL_MESSAGE_TYPE
    case 3:
      // 三方消息违规
      attachment.type = OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE
      attachment.customType = OneOnOneChatCustomMessageType.PRIVACY_RISK_MESSAGE_TYPE
    case 4:
      // 通用违规消息
      attachment.type = OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE
      attachment.customType = OneOnOneChatCustomMessageType.COMMON_RISK_MESSAGE_TYPE
    default:
      attachment.type = OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TIPS_TYPE
      attachment.customType = OneOnOneChatCustomMessageType.ACCOST_MESSAGE_TIPS_TYPE
      attachment.cellHeight = 30 + 20
    }

    let message = MessageUtils.customMessage(text: "", rawAttachment: attachment.encode())
    if let antiSpamMessage = antiSpamMessage {
      message.createTime = antiSpamMessage.createTime + 1
    }

    viewModel.chatRepo.insertMessageToLocal(message: message, conversationId: viewModel.conversationId) {[weak self] meaasge, error in
       print("send custom message error : ", error?.localizedDescription as Any)
        // 当前聊天页面插入的提示消息
        self?.viewModel.modelFromMessage(message: message) { model in
          if let index = self?.viewModel.insertToMessages(model) {
            self?.viewModel.delegate?.sending(message, IndexPath(row: index, section: 0))
          }
        }
    }
  }
  
  public func onReceiveGift(gift: NEOneOnOneOneGift) {
    if gift.senderUserUuid != viewModel.conversationId {
      return
    }
    playGiftWithName(name: "anim_gift_0\(gift.giftId)")
  }

  // MARK: override

  override public func didTapReeditButton(_ cell: UITableViewCell, _ model: MessageContentModel?) {
    super.didTapReeditButton(cell, model)
    // 判断当前是否是语音输入中状态
    if audioExchangeButton.isSelected {
      clickAudioExchangeButton()
    }
  }

  override public func recallMessage() {
    if let message = viewModel.operationModel?.message {
      if let yidun = message.antispamConfig, let result = yidun.antispamCheating, result.count > 0 {
        NEOneOnOneToast.show(ne_localized("敏感消息，撤回失败"))
        return
      }
    }
    super.recallMessage()
  }

  override public func didSelectMoreCell(cell: NEInputMoreCell) {
    if let type = cell.cellData?.type, type == .takePicture {
      // 判断是否在小窗：弹出Toast
      if let canContinueAction = NEOneOnOneUIManager.sharedInstance().canContinueAction,
         !canContinueAction() {
        /// 不能操作
        return
      }
    }
    super.didSelectMoreCell(cell: cell)
  }

  override public func layoutInputView(offset: CGFloat, _ scrollToBottom: Bool = false) {
    super.layoutInputView(offset: offset, scrollToBottom)
    if offset == 0 {
      // 更多按钮旋转
      moreButton.isSelected = false
      emojiButton.isSelected = false
    }
  }

  override public func sendText(text: String?, attribute: NSAttributedString?) {
    super.sendText(text: text, attribute: attribute)
    if let text = text, text.count <= 0 {
      NEOneOnOneToast.show("发送内容为空")
    }
  }

  override public func keyBoardWillShow(_ notification: Notification) {
    super.keyBoardWillShow(notification)
    /// 更多的箭头变为未选中
    emojiButton.isSelected = false
    moreButton.isSelected = false
  }

  override public func getSessionInfo(sessionId: String, _ completion: @escaping () -> Void) {
    super.getSessionInfo(sessionId: sessionId, completion)

    viewModel.contactRepo.getUserWithFriend(accountIds: [viewModel.sessionId]) { [weak self] users, error in
      self?.headerView.navigationItemView.userNameLabel.text = users?.first?.showName()
    }

//    let text = ne_localized("请输入信息")
//    let attribute = NSMutableAttributedString(string: text)
//    let style = NSMutableParagraphStyle()
//    style.lineBreakMode = .byTruncatingTail
//    style.alignment = .left
//    attribute.addAttribute(.font, value: UIFont.systemFont(ofSize: 16), range: NSMakeRange(0, text.utf16.count))
//    attribute.addAttribute(.foregroundColor, value: UIColor.gray, range: NSMakeRange(0, text.utf16.count))
//    attribute.addAttribute(.paragraphStyle, value: style, range: NSMakeRange(0, text.utf16.count))
//    chatInputView.textView.attributedPlaceholder = attribute
  }

  override public func remoteUserEditing() {
    super.remoteUserEditing()
    DispatchQueue.main.async { [weak self] in
      self?.remoteUserIsEditing = true
      self?.headerView.navigationItemView.userInputingLabel.isHidden = false
      self?.headerView.navigationItemView.onlineLabel.isHidden = true
      self?.headerView.navigationItemView.onlineImageView.isHidden = true
    }
  }

  override public func remoteUserEndEditing() {
    super.remoteUserEndEditing()
    DispatchQueue.main.async {
      self.remoteUserIsEditing = false
      self.headerView.navigationItemView.userInputingLabel.isHidden = true
      if self.remoteUserIsOnline {
        self.headerView.navigationItemView.onlineLabel.isHidden = false
        self.headerView.navigationItemView.onlineImageView.isHidden = false
      } else {
        self.headerView.navigationItemView.onlineLabel.isHidden = true
        self.headerView.navigationItemView.onlineImageView.isHidden = true
      }
    }
  }

  override public func didLongPressMessageView(_ cell: UITableViewCell, _ model: MessageContentModel?) {
    super.didLongPressMessageView(cell, model)
  }

  override public func setOperationItems(items: inout [OperationItem], model: MessageContentModel?) {
    if let message = model?.message,
       message.messageType == .MESSAGE_TYPE_CUSTOM,
       let jsonString = message.attachment?.raw {
      let attachment = CustomAttachment(customJsonSrting: jsonString)
      if attachment.type == OneOnOneChatCustomMessageType.SEND_GIFT_TYPE ||
        attachment.type == OneOnOneChatCustomMessageType.OFFICIAL_GIFT_TYPE {
        items = [
          OperationItem.deleteItem(),
        ]
      }
    } else if let message = model?.message,
              message.messageType == .MESSAGE_TYPE_TEXT {
      // 文本消息
      var addItems: [OperationItem] = []
      for item in items {
        if item.type == .copy || item.type == .delete || item.type == .recall {
          addItems.append(item)
        }
      }
      items = addItems
    } else if let message = model?.message,
              message.messageType == .MESSAGE_TYPE_AUDIO {
      // 音频消息
      var addItems: [OperationItem] = []
      for item in items {
        if item.type == .delete || item.type == .recall {
          addItems.append(item)
        }
      }
      items = addItems

    } else if let message = model?.message,
              message.messageType == .MESSAGE_TYPE_IMAGE {
      // 音频消息
      var addItems: [OperationItem] = []
      for item in items {
        if item.type == .delete || item.type == .recall {
          addItems.append(item)
        }
      }
      items = addItems
    } else if let message = model?.message,
              message.messageType == .MESSAGE_TYPE_VIDEO {
      // 视频消息
      var addItems: [OperationItem] = []
      for item in items {
        if item.type == .delete || item.type == .recall {
          addItems.append(item)
        }
      }
      items = addItems
    } else if let message = model?.message,
              message.messageType == .MESSAGE_TYPE_LOCATION {
      // 定位消息
      var addItems: [OperationItem] = []
      for item in items {
        if item.type == .delete || item.type == .recall {
          addItems.append(item)
        }
      }
      items = addItems
    }
  }

  // MARK: UIPopoverPresentationControllerDelegate

  public func adaptivePresentationStyle(for controller: UIPresentationController) -> UIModalPresentationStyle {
    .none
  }

  // MARK: Engine Call

  // 具体处理

  func dealWithAction(_ isAudio: Bool) {
    viewModel.contactRepo.getUserListFromCloud(accountIds: [viewModel.sessionId]) { [weak self] users, error in
      let user = users?.first
      if let weakself = self {
        NEOneOnOneUIKitCallEngine.getInstance.dealWithAction(weakself, sessionId: weakself.viewModel.sessionId, isAudio: isAudio, isVirtualRoom: weakself.remoteUserInfo?.oc_callType == 1, nickName: user?.user?.name, icon: weakself.remoteUserInfo?.icon, audioUrl: weakself.remoteUserInfo?.audioUrl, videoUrl: weakself.remoteUserInfo?.videoUrl)
      }
    }
  }

  deinit {
    print("NEOneOnOneChatP2PViewController控制器释放")
    /// 取消订阅
    NIMSDK.shared().subscribeManager.unSubscribeEvent(subscribeRequest)
    NIMSDK.shared().mediaManager.remove(self)
    viewModel.delegate = nil
  }

  // MARK: NIMEventSubscribeManagerDelegate

  public func onRecvSubscribeEvents(_ events: [Any]) {
    for event in events {
      if let onlineEvent = event as? NIMSubscribeEvent, onlineEvent.type == 1 {
        /// 订阅的在线事件
        if onlineEvent.value == NIMSubscribeEventOnlineValue.login.rawValue {
          DispatchQueue.main.async {
            self.remoteUserIsOnline = true
            if self.remoteUserIsEditing {
              // 对方输入中，不做处理
              return
            }
            self.headerView.navigationItemView.onlineLabel.isHidden = false
            self.headerView.navigationItemView.onlineImageView.isHidden = false
          }
        } else {
          DispatchQueue.main.async {
            self.remoteUserIsOnline = false
            if self.remoteUserIsEditing {
              // 对方输入中，不做处理
              return
            }
            self.headerView.navigationItemView.onlineLabel.isHidden = true
            self.headerView.navigationItemView.onlineImageView.isHidden = true
          }
        }
      }
    }
  }

  override public func sending(_ message: V2NIMMessage, _ index: IndexPath) {
    super.sending(message, index)
    message.antispamConfig?.antispamEnabled = true
  }

  override public func sendSuccess(_ message: V2NIMMessage, _ index: IndexPath) {
    super.sendSuccess(message, index)
  }
}

public class NEOneOnOnePopView: UIView {
  public var reportAction: (() -> Void)?
  public var blockAction: (() -> Void)?

  private var _isInBlack = false
  public var isInBlack: Bool {
    set {
      blockButton.setTitle(newValue ? ne_localized("取消拉黑") : ne_localized("拉黑"), for: .normal)
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
    reportButton.setTitle(ne_localized("举报"), for: UIControl.State.normal)
    reportButton.setTitleColor(UIColor(hexString: "#333333"), for: .normal)
    reportButton.titleLabel?.font = UIFont(name: "PingFangSC-Regular", size: 14)
    reportButton.addTarget(self, action: #selector(clickReportButton), for: .touchUpInside)
    reportButton.accessibilityIdentifier = "id.report"
    return reportButton
  }()

  lazy var blockButton: UIButton = {
    let blockButton = UIButton()
    blockButton.setTitle(_isInBlack ? ne_localized("取消拉黑") : ne_localized("拉黑"), for: UIControl.State.normal)
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
    reportBackImageView.image = ne_chatUI_imageName(imageName: "report_back_icon")
    reportBackImageView.contentMode = .scaleToFill
    reportBackImageView.backgroundColor = UIColor.clear
    return reportBackImageView
  }()

  private func updateViews() {
    reportBackImageView.snp.updateConstraints { make in
      make.top.equalTo(sourceRect.origin.y + sourceRect.size.height / 2 + 3)
      make.right.equalTo(self).offset(-18)
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
