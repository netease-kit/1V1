// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

import Foundation

func makeAttributeString() -> NSAttributedString {
  // 创建一个字符串
  let string = "Hello, World!"

  // 创建一个 NSMutableAttributedString 对象，并将字符串设置为其初始值
  let attributedString = NSMutableAttributedString(string: string)

  // 创建一个边框
  let border = CALayer()
  border.frame = CGRect(x: 0, y: 0, width: attributedString.size().width, height: attributedString.size().height)
  border.borderWidth = 1.0
  border.borderColor = UIColor.black.cgColor

  // 将边框添加到 NSAttributedString 的 layer 上
  let layer = CALayer()
  layer.frame = CGRect(x: 0, y: 0, width: attributedString.size().width, height: attributedString.size().height)
  layer.addSublayer(border)

  // 将 layer 转换为 UIImage
  UIGraphicsBeginImageContextWithOptions(layer.frame.size, false, 0.0)
  layer.render(in: UIGraphicsGetCurrentContext()!)
  let image = UIGraphicsGetImageFromCurrentImageContext()
  UIGraphicsEndImageContext()

  // 将 UIImage 应用于 NSAttributedString
  let imageAttachment = NSTextAttachment()
  imageAttachment.image = image
  let imageString = NSAttributedString(attachment: imageAttachment)
  attributedString.append(imageString)

  // 最终的 NSAttributedString
  let finalAttributedString = NSAttributedString(attributedString: attributedString)
  return finalAttributedString
}
