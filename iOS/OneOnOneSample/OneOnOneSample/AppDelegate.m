// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "AppDelegate.h"
// #import <Bugly/Bugly.h>
//#import <Hawk/Hawk.h>
//#import <NEJsonModel/NEJsonModel.h>
#import "AppDelegate+OneOnOne.h"
#import "AppDelegate+OneOnOneView.h"
#import "AppKey.h"

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // Override point for customization after application launch.
  [self vr_initWindow];
  [self setUpOneOnOneSDK];

  //  [Bugly startWithAppId:@"6a2df8f127"];
  [application setIdleTimerDisabled:YES];
  return YES;
}
- (UIInterfaceOrientationMask)application:(UIApplication *)application
    supportedInterfaceOrientationsForWindow:(nullable UIWindow *)window {
  return UIInterfaceOrientationMaskPortrait;
}

@end
