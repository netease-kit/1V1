// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import "NEOneOnOneCallViewController.h"

NS_ASSUME_NONNULL_BEGIN

typedef BOOL (^NEOneOnOneUIInterceptor)(void);

typedef NSString *_Nullable (^NEOneOnOneUITransducer)(void);

@interface NEOneOnOneUIKitEngine : NSObject

// 拦截器 ，是否拦截邀请消息
@property(nonatomic, copy) NEOneOnOneUIInterceptor interceptor;

// 获取本地状态是否可以拨打，传入值为提示Toast
@property(nonatomic, copy) NEOneOnOneUITransducer canCall;

/// 单例对象
+ (NEOneOnOneUIKitEngine *)sharedInstance;

/// 添加NERTC监听
- (void)addObserve;
/// 移出监听
- (void)removeObserve;

/// 是否在1v1房间中
- (BOOL)isInOneInOne;

/// 状态机处理
- (void)setRTCIdle;
- (void)setRTCCaling;

@end

NS_ASSUME_NONNULL_END
