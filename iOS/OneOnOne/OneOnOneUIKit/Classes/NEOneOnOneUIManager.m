// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUIManager.h"
#import "NECallEngine+Party.h"
#import "NEOneOnOneLog.h"

@interface NEOneOnOneUIManager () <NEOneOnOneAuthListener>

@end

@implementation NEOneOnOneUIManager

+ (NEOneOnOneUIManager *)sharedInstance {
  static NEOneOnOneUIManager *instance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[NEOneOnOneUIManager alloc] init];
    [[NEOneOnOneKit getInstance] addAuthListener:instance];
  });
  return instance;
}
- (void)initializeWithConfig:(NEOneOnOneKitConfig *)config
                    callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback {
  self.config = config;
  [[NEOneOnOneKit getInstance] initialize:config callback:callback];
  [NEOneOnOneLog setUp:config.appKey];
}

- (void)loginWithAccount:(NSString *)account
                   token:(NSString *)token
                 imToken:(NSString *)imToken
                nickname:(NSString *)nickname
                  avatar:(NSString *)avatar
             resumeLogin:(BOOL)resume
                callback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback {
  self.nickname = nickname;
  [[NEOneOnOneKit getInstance] login:account
                            nickName:nickname
                              avatar:avatar
                               token:token
                             imToken:imToken
                         resumeLogin:resume
                            callback:callback];
}

- (void)logoutWithCallback:(void (^)(NSInteger, NSString *_Nullable, id _Nullable))callback {
  [NEOneOnOneKit.getInstance logoutWithCallback:callback];
}

- (void)onOneOnOneAuthEvent:(enum NEOneOnOneAuthEvent)event {
  if ([self.delegate respondsToSelector:@selector(onOneOnOneClientEvent:)]) {
    [self.delegate onOneOnOneClientEvent:event];
  }
}

/// 是否在1v1房间中
- (BOOL)isInOneOnOne {
  return [[NEOneOnOneKit getInstance] isInOneOnOne];
}

/// 状态机处理
- (void)setRTCIdle {
  [[NECallEngine sharedInstance] changeStatusIdle];
}
- (void)setRTCCaling {
  [[NECallEngine sharedInstance] changeStatusCalling];
}

@end
