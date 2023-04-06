// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^ClickAction)(void);

@interface NEOneOnOneBottomPresentView : UIView
// 展示
- (void)show:(void (^)(void))complete;
// 隐藏
- (void)dismiss:(void (^)(void))complete;
// 点击音频通话
@property(nonatomic, copy) ClickAction clickAudioAction;
// 点击视频通话
@property(nonatomic, copy) ClickAction clickVideoAction;
@end

;
NS_ASSUME_NONNULL_END
