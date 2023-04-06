// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUIManager.h"
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

//- (bool)isLoggedIn {
//  return [[NEOneOnOneKit getInstance] isLoggedIn];
//    return  NO;
//}

- (void)onOneOnOneAuthEvent:(enum NEOneOnOneAuthEvent)event {
  if ([self.delegate respondsToSelector:@selector(onOneOnOneClientEvent:)]) {
    [self.delegate onOneOnOneClientEvent:event];
  }
}

- (UINavigationController *)createViewController {
  UINavigationController *c =
      [[UINavigationController alloc] initWithRootViewController:[[UIViewController alloc] init]];
  c.modalPresentationStyle = UIModalPresentationFullScreen;
  return c;
}

- (UINavigationController *)roomListViewController {
  UINavigationController *c =
      [[UINavigationController alloc] initWithRootViewController:[[UIViewController alloc] init]];
  if (@available(iOS 13.0, *)) {
    c.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
  }
  c.modalPresentationStyle = UIModalPresentationFullScreen;
  return c;
}

@end
