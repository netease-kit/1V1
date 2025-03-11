// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

let tag = "NEOneOnOnePlayerUtil"

@objc
public protocol NEOneOnOnePlayerProtocol: NSObjectProtocol {
  /// 播放结束
  @objc optional func OneOnOnePlayerDidFinishPlaying()
  /// 播放错误
  @objc optional func OneOnOnePlayerErrorPlaying()
  /// 播放打断
  @objc optional func OneOnOnePlayerInterruption(_ notification: Notification)
}

@objcMembers
public class NEOneOnOnePlayerUtil: NSObject {
  private static let instance = NEOneOnOnePlayerUtil()

  /// 单例初始化
  /// - Returns: 单例对象
  public static func getInstance() -> NEOneOnOnePlayerUtil {
    instance
  }

  /// 播放器
  private var player: AVPlayer?

  // 监听器数组
  var listeners = NSPointerArray.weakObjects()

  /// 保存的原始session category
  var categray: AVAudioSession.Category?

  /// 添加播放器监听
  /// - Parameter listener: 事件监听

  public func addPlayerListener(_ listener: NEOneOnOnePlayerProtocol) {
    listeners.addWeakObject(listener)
  }

  /// 移除播放器监听
  public func removePlayerListener(_ listener: NEOneOnOnePlayerProtocol) {
    listeners.removeWeakObject(listener)
  }

  public func setOriginalCategray(_ value: AVAudioSession.Category? = nil) {
    if let value = value {
      categray = value
    }
  }

  public func resetCategray() {
    guard let categray = categray else { return }

    do {
      try AVAudioSession.sharedInstance().setCategory(categray)
      self.categray = nil
    } catch {
      NEOneOnOneLog.infoLog(tag, desc: "resetCategray  error \(error.localizedDescription)")
    }
  }

  public func changePlayerModelSpeaker(_ speaker: Bool = true) {
    if speaker {
      do {
        try AVAudioSession.sharedInstance().setCategory(.playback)
        try AVAudioSession.sharedInstance().overrideOutputAudioPort(.speaker)
      } catch {
        NEOneOnOneLog.infoLog(tag, desc: "changePlayerModelSpeaker speaker error \(error.localizedDescription)")
      }

    } else {
      do {
        try AVAudioSession.sharedInstance().setCategory(.playAndRecord)
        try AVAudioSession.sharedInstance().overrideOutputAudioPort(.none)
      } catch {
        NEOneOnOneLog.infoLog(tag, desc: "changePlayerModelSpeaker none error \(error.localizedDescription)")
      }
    }

    try? AVAudioSession.sharedInstance().setActive(true)
  }

  // MARK: player

  // 重置播放器
  public func resetPlayer() {
    player = nil
  }

  // 设置播放播放音量
  public func setPlayerVolume(_ volume: Float) {
    player?.volume = volume
  }

  // 播放器播放
  // 前后台切换可以手动调用，启动的时候视频/音频自动播放
  public func playerPlay() {
    player?.play()
  }

  // 播放器暂停
  // 前后台切换可以手动调用，启动的时候视频/音频自动播放
  public func playerPause() {
    player?.pause()
  }

  // 播放音频
  public func playAudio(_ urlString: String) {
    do {
      try AVAudioSession.sharedInstance().setCategory(.playAndRecord, mode: .videoChat, options: .allowBluetooth)
    } catch {
      NEOneOnOneLog.infoLog(tag, desc: "设置AVAudioSession错误: \(tag) \(error.localizedDescription)")
    }
    guard let audioUrl = URL(string: urlString) else {
      return
    }
    player = AVPlayer(url: audioUrl)
    player?.play()
    changePlayerModelSpeaker()
    changePlayerModelSpeaker(false)
    NotificationCenter.default.addObserver(self,
                                           selector: #selector(playerDidFinishPlaying),
                                           name: .AVPlayerItemDidPlayToEndTime,
                                           object: player?.currentItem)
  }

  // 播放视频
  public func playVideo(_ urlString: String, frame: CGRect, superLayer: CALayer) {
    guard let videoURL = URL(string: urlString) else { return }
    player = AVPlayer(url: videoURL)
    let playerLayer = AVPlayerLayer(player: player)
    playerLayer.frame = frame
    playerLayer.videoGravity = .resizeAspectFill
    superLayer.addSublayer(playerLayer)
    player?.play()
    changePlayerModelSpeaker()

    NotificationCenter.default.addObserver(self, selector: #selector(playerDidFinishPlaying), name: .AVPlayerItemDidPlayToEndTime, object: player?.currentItem)
    NotificationCenter.default.addObserver(self, selector: #selector(playerErrorPlaying), name: .AVPlayerItemFailedToPlayToEndTime, object: player?.currentItem)
    NotificationCenter.default.addObserver(self, selector: #selector(playerErrorPlaying), name: .AVPlayerItemPlaybackStalled, object: player?.currentItem)
    NotificationCenter.default.addObserver(self, selector: #selector(playerErrorPlaying), name: .AVPlayerItemNewErrorLogEntry, object: player?.currentItem)
    NotificationCenter.default.addObserver(self, selector: #selector(playerInterruption(_:)), name: AVAudioSession.interruptionNotification, object: nil)
  }

  // MARK: objc method

  // 播放发生错误
  func playerErrorPlaying() {
    // 播放错误
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEOneOnOnePlayerProtocol, let listener = pointerListener as? NEOneOnOnePlayerProtocol else { continue }

      if listener.responds(to: #selector(NEOneOnOnePlayerProtocol.OneOnOnePlayerErrorPlaying)) {
        listener.OneOnOnePlayerErrorPlaying?()
      }
    }
  }

  // 播放打断
  func playerInterruption(_ notification: Notification) {
    // 播放结束后的处理
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEOneOnOnePlayerProtocol, let listener = pointerListener as? NEOneOnOnePlayerProtocol else { continue }

      if listener.responds(to: #selector(NEOneOnOnePlayerProtocol.OneOnOnePlayerInterruption(_:))) {
        listener.OneOnOnePlayerInterruption?(notification)
      }
    }
  }

  /// 播放结束
  func playerDidFinishPlaying() {
    // 播放结束后的处理
    for pointerListener in listeners.allObjects {
      guard pointerListener is NEOneOnOnePlayerProtocol, let listener = pointerListener as? NEOneOnOnePlayerProtocol else { continue }

      if listener.responds(to: #selector(NEOneOnOnePlayerProtocol.OneOnOnePlayerDidFinishPlaying)) {
        listener.OneOnOnePlayerDidFinishPlaying?()
      }
    }
  }

  /// 是否插入耳机
  public func isHeadSetPlugging() -> Bool {
    let route = AVAudioSession.sharedInstance().currentRoute
    var isHead = false
    for desc in route.outputs {
      switch desc.portType {
      case .headphones, .bluetoothA2DP, .usbAudio, .bluetoothHFP:
        isHead = true
      default: break
      }
    }
    return isHead
  }
}

/// NSPointerArray 扩展
extension NSPointerArray {
  func addWeakObject<T: NSObjectProtocol>(_ object: T?) {
    guard let weakObjc = object else { return }
    let pointer = Unmanaged.passUnretained(weakObjc).toOpaque()
    objc_sync_enter(self)
    addPointer(pointer)
    objc_sync_exit(self)
  }

  func removeWeakObject<T: NSObjectProtocol>(_ object: T?) {
    objc_sync_enter(self)
    var listenerIndexArray = [Int]()
    for index in 0 ..< allObjects.count {
      let pointerListener = pointer(at: index)
      // 过滤nil
      guard let tempPointer = pointerListener else { continue }
      let tempListener = Unmanaged<T>.fromOpaque(tempPointer).takeUnretainedValue()
      if tempListener.isEqual(object) {
        listenerIndexArray.append(index)
      }
    }
    for listenerIndex in listenerIndexArray {
      if listenerIndex < allObjects.count {
        removePointer(at: listenerIndex)
      }
    }
    objc_sync_exit(self)
  }
}
