// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NEOneOnOneUIKit/NEOneOnOneUIKitEngine.h>
#import <NEOneOnOneUIKit/NEOneOnOneUIManager.h>
#import "AppDelegate+OneOnOne.h"
#import "AppKey.h"

@interface AppDelegate (OneOnOne) <NEOneOnOneUIDelegate>

@end

@implementation AppDelegate (OneOnOne)
- (NSString *)getAppkey {
  BOOL isOutsea = isOverSea;
  if (isOverSea) {
    return APP_KEY_OVERSEA;
  } else {
    return APP_KEY_MAINLAND;
  }
}

- (void)setUpOneOnOneSDK {
  NEOneOnOneKitConfig *config = [[NEOneOnOneKitConfig alloc] init];
  config.appKey = [self getAppkey];
  if (isOverSea) {
    config.extras = @{@"serverUrl" : @"oversea"};
  }

  [[NEOneOnOneUIManager sharedInstance]
      initializeWithConfig:config
                  callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                    if (code != 0) return;
                    [[NEOneOnOneUIManager sharedInstance]
                        loginWithAccount:accountId
                                   token:accessToken
                                 imToken:imToken
                                nickname:nickname
                                  avatar:avatar
                             resumeLogin:NO
                                callback:^(NSInteger code, NSString *_Nullable msg,
                                           id _Nullable obj) {
                                  if (code != 0) {
                                    NSLog(@"登录失败");
                                  } else {
                                    // 启动添加监听
                                    [[NEOneOnOneUIKitEngine sharedInstance] addObserve];
                                    // 是否可以播放
                                    [NEOneOnOneUIKitEngine sharedInstance].canCall =
                                        ^NSString *_Nullable {
                                      /// 可以拨打
                                      return nil;
                                    };

                                    [NEOneOnOneUIKitEngine sharedInstance].interceptor = ^BOOL {
                                      return NO;
                                    };
                                  }
                                }];
                  }];
}

- (void)onOneOnOneClientEvent:(NEOneOnOneClientEvent)event {
}

@end
