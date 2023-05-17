// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEUIKit/NEUIBaseViewController.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^PrivateLatterBlock)(NSString *sessionId);

/// 1v1列表
@interface NEOneOnOneRoomListViewController : NEUIBaseViewController

@property(nonatomic, copy) PrivateLatterBlock privateLatter;
@end

NS_ASSUME_NONNULL_END
