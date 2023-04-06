// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^ButtonClicked)(void);

@interface NEOneOnOneCustomButtonView : UIView

/// 按钮点击回调
@property(nonatomic, copy) ButtonClicked buttonClicked;

// 设置按钮图片
- (void)setButtonImage:(NSString *)imageName;
- (void)setTextLabel:(NSString *)text;

@end

NS_ASSUME_NONNULL_END
