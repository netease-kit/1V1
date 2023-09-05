// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation
import LottieSwift

public typealias CompleteBlock = () -> Void

class AnimationDelegate: NSObject, CAAnimationDelegate {
  var completeBlock: CompleteBlock?
  func animationDidStop(_ anim: CAAnimation, finished flag: Bool) {
    if let completeBlock = completeBlock {
      completeBlock()
    }
  }
}

public class NEOneOnOneAudioInputingView: UIView {
  // 是否取消发送标记
  var needCancel: Bool = false {
    didSet {
      reloadSubViews()
    }
  }

  var completeBlock: CompleteBlock?

  // 时长
  let duration: Double = 61

  override public init(frame: CGRect) {
    super.init(frame: frame)
    backgroundColor = UIColor(hexString: "#000000", 0.7)
    addSubview(audioInputImageView)
    audioInputImageView.snp.makeConstraints { make in
      make.centerX.equalTo(self)
      make.bottom.equalTo(self).offset(-196)
      make.height.width.equalTo(100)
    }
    addSubview(audioInputLabel)
    audioInputLabel.snp.makeConstraints { make in
      make.centerX.equalTo(self)
      make.top.equalTo(audioInputImageView.snp_bottom).offset(16)
    }

    addSubview(lottieView)
    lottieView.snp.makeConstraints { make in
      make.left.right.top.bottom.equalTo(audioInputImageView)
    }
    layoutIfNeeded()

    let shapeLayer = CAShapeLayer()
    let center = audioInputImageView.center
    let circularPath = UIBezierPath(arcCenter: center, radius: 51, startAngle: -CGFloat.pi / 2, endAngle: 2 * CGFloat.pi - CGFloat.pi / 2, clockwise: true)

    shapeLayer.path = circularPath.cgPath
    shapeLayer.strokeColor = UIColor(hexString: "#FB6E79").cgColor
    shapeLayer.lineWidth = 2
    shapeLayer.fillColor = UIColor.clear.cgColor
    shapeLayer.lineCap = CAShapeLayerLineCap.round
    shapeLayer.strokeEnd = 0

    layer.addSublayer(shapeLayer)

    let animation = CABasicAnimation(keyPath: "strokeEnd")
    animation.fromValue = 0
    animation.toValue = 1
    let animationDelegate = AnimationDelegate()
    animationDelegate.completeBlock = {
      if let completeBlock = self.completeBlock {
        completeBlock()
        self.removeFromSuperview()
      }
    }
    animation.delegate = animationDelegate

    animation.duration = duration
    animation.fillMode = CAMediaTimingFillMode.forwards
    animation.isRemovedOnCompletion = false

    shapeLayer.add(animation, forKey: "strokeAnimation")
    lottieView.play()
  }

  required init?(coder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  lazy var audioInputImageView: UIImageView = {
    let audioInputImageView = UIImageView()
    audioInputImageView.backgroundColor = UIColor.white
    audioInputImageView.contentMode = .center
    audioInputImageView.layer.masksToBounds = true
    audioInputImageView.layer.cornerRadius = 50
    audioInputImageView.image = ne_chatUI_imageName(imageName: "")
    return audioInputImageView
  }()

  lazy var audioInputLabel: UILabel = {
    let audioInputLabel = UILabel()
    audioInputLabel.text = ne_localized("松开发送")
    audioInputLabel.font = UIFont(name: "PingFangSC-Regular", size: 15)
    audioInputLabel.textColor = UIColor.white
    return audioInputLabel

  }()

  func reloadSubViews() {
    if needCancel {
      audioInputImageView.image = ne_chatUI_imageName(imageName: "audio_inputing_cancel_icon")
      audioInputLabel.text = ne_localized("松开取消")
      lottieView.isHidden = true
      lottieView.stop()
    } else {
      audioInputImageView.image = ne_chatUI_imageName(imageName: "")
      lottieView.isHidden = false
      lottieView.play()
      audioInputLabel.text = ne_localized("松开发送")
    }
  }

  lazy var lottieView: NELottieView = {
    let path = Bundle.main.path(forResource: "Frameworks/NEOneOnOneChatUIKit.framework/NEOneOnOneChatUIKit", ofType: "bundle")
    let lottie = NELottieView(frame: CGRect(x: 0, y: 0, width: 100, height: 100), lottie: "audio_data", bundle: NEOneOnOneChatUtil.ne_localBundle())
    lottie.stop()
    lottie.layer.masksToBounds = true
    lottie.layer.cornerRadius = 50
//        lottie.backgroundColor = .red
    return lottie
  }()
}
