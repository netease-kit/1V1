// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>
#import "NEOneOnOneUIKitMacro.h"
NS_ASSUME_NONNULL_BEGIN

@interface NEOneOnOneVideoConnectedView : UIView

// 小窗口
@property(nonatomic, strong) UIView *localVideoView;
// 大窗口
@property(nonatomic, strong) UIView *remoteVideoView;

/// 是否可以进行大小窗切换
@property(nonatomic, assign) BOOL canChangeView;

/// 按钮点击回调 增加参数
@property(nonatomic, copy) ItemEventExpand itemExpand;

/// 刷新UI
- (void)updateUI:(NSString *)remoteIcon remoteName:(NSString *)name;
/// 刷新定时器
- (void)updateTimer:(NSString *)time;

/// 是否展示大图黑图
- (void)showRemoteBlackView:(BOOL)show;
/// 是否展示小图黑图
- (void)showLocalBlackView:(BOOL)show;

/// 小图添加高斯模糊
- (void)showSmallEffectView:(BOOL)show;
/// 大图添加高斯模糊
- (void)showLargeEffectView:(BOOL)show;
@end

NS_ASSUME_NONNULL_END
