// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/// 登录事件枚举
typedef NS_ENUM(NSInteger, NEOneOnOneClientEvent) {
  /// 被踢出登录
  NEOneOnOneClientEventKicOut,
  /// 服务器禁止登录
  NEOneOnOneClientEventForbidden,
  /// 账号或密码错误
  NEOneOnOneClientEventAccountTokenError,
  /// 登录成功
  NEOneOnOneClientEventLoggedIn,
  /// 未登录
  NEOneOnOneClientEventLoggedOut,
  /// 授权错误
  NEOneOnOneClientEventIncorrectToken,
  /// Token过期
  NEOneOnOneClientEventTokenExpored,
};

@protocol NEOneOnOneUIDelegate <NSObject>

- (void)onOneOnOneClientEvent:(NEOneOnOneClientEvent)event;

@end

@interface NEOneOnOneUIManager : NSObject

@property(nonatomic, copy) NSString *nickname;

//@property(nonatomic, assign, readonly) bool isLoggedIn;

@property(nonatomic, weak) id<NEOneOnOneUIDelegate> delegate;

@property(nonatomic, strong) NEOneOnOneKitConfig *config;

+ (NEOneOnOneUIManager *)sharedInstance;

- (void)initializeWithConfig:(NEOneOnOneKitConfig *)config
                    callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback;

- (void)loginWithAccount:(NSString *)account
                   token:(NSString *)token
                 imToken:(NSString *)imToken
                nickname:(NSString *)nickname
                  avatar:(NSString *)avatar
             resumeLogin:(BOOL)resume
                callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback;

- (void)logoutWithCallback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback;

/// 房间创建界面
- (UINavigationController *)createViewController;

/// 房间列表页
- (UINavigationController *)roomListViewController;

@end

NS_ASSUME_NONNULL_END
