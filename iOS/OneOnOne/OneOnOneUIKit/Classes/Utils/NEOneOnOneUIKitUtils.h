// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <AVFoundation/AVFoundation.h>
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NEOneOnOneUIKitUtils : NSObject

// 获取当前时间戳
+ (NSString *)currentTimeStr;

/**持续时间*/
+ (long long)getDurationStartTime:(NSString *)startTime endTime:(NSString *)endTime;

// 权限判断，是否被拒/被关闭
+ (BOOL)permisionDenied:(AVAuthorizationStatus)AVstatus;

// 权限申请
+ (void)getMicrophonePermissions:(AVMediaType)meidaType
                        complete:(void (^)(BOOL authorized))completion;

/// 获取当前视图控制器
+ (UIViewController *)findVisibleViewController;
@end

NS_ASSUME_NONNULL_END
