// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NERtcCallKit/NERtcCallKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface NERtcCallKit (Party)

// 此文件单纯为了处理NERTCCallKit 私有方法调用
// 原因：PSTN超时状态机未重置
// 呼叫中状态
- (void)changeStatusCalling;

/// 空闲状态
- (void)changeStatusIdle;
@end

NS_ASSUME_NONNULL_END
