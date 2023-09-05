// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NEOneOnOneUIKitMacro.h"
NS_ASSUME_NONNULL_BEGIN

@interface NEOneOnOneConnectingView : UIView

/// 按钮点击回调
@property(nonatomic, copy) ItemEvent itemEvent;
/// 按钮点击回调 增加参数
@property(nonatomic, copy) ItemEventExpand itemExpand;

- (void)setScene:(BOOL)isAudio;

/// 根据状态 刷新UI
- (void)updateUI:(NSString *)remoteIcon remoteName:(NSString *)name status:(NECallViewStatus)status;
/// 刷新定时器计数
- (void)updateTimer:(NSString *)time;

@end

NS_ASSUME_NONNULL_END
