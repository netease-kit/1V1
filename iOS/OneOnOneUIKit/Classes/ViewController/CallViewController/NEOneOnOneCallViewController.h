// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NERtcCallKit/NERtcCallKit.h>
#import <UIKit/UIKit.h>
#import "NEOneOnOneConnectingView.h"
#import "NEOneOnOneVideoConnectedView.h"
NS_ASSUME_NONNULL_BEGIN

static NSString *tag = @"NEOneOnOneCallViewController";

typedef void (^BusyBlock)(void);

@interface NEOneOnOneCallViewController : UIViewController <NERtcLinkEngineDelegate,
                                                            NERtcCallKitDelegate,
                                                            NEOneOnOneListener,
                                                            NERtcEngineDelegateEx>
@property(nonatomic, strong) NEOneOnOneConnectingView *connectingView;
@property(nonatomic, strong) NEOneOnOneVideoConnectedView *videoConnectedView;
@property(nonatomic, strong) UIButton *giftButton;

// 是否已经由于其他原因挂断，并关闭房间，如果是，在endroom的时候不需要再跳出toast
@property(nonatomic, strong) NSString *hasEndRoom;

/// 房间关闭的时候需要展示的文本
@property(nonatomic, strong) NSString *needShowStringWhenEndRoom;

#pragma mark 外部设置参数
@property(nonatomic, assign) NEEnterStatus enterStatus;

// 远端账号信息
@property(nonatomic, strong) NEOneOnOneOnlineUser *remoteUser;

// 远端RTC id信息,同remoteUser内的内容不同，remoteUser是IM内的信息，这个是RTC的内容,屏蔽音频需要
@property(nonatomic, assign) uint64_t remoteUserId;

@property(nonatomic, copy) BusyBlock busyBlock;

/// 关闭房间
- (void)endRoom;
// 远端用户同意 刷新UI(Video和Audio) ； Audio逻辑处理，不需要关心用户是否加入RTC
- (void)userAccept;

// 远端用户加入RTC房间，用于Video 视图展示刷新
- (void)userEnterVideo;

/// 摄像头变化回调
- (void)onVideoMute:(BOOL)mute userID:(NSString *)userID;

@end

NS_ASSUME_NONNULL_END
