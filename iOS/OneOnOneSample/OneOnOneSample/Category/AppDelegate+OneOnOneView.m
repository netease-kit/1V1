// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEOneOnOneRoomListViewController.h>
#import <NEOneOnOneUIKit/NEOneOnOneUIKitEngine.h>
#import <NEUIKit/NEUIBackNavigationController.h>
#import <UIKit/UIKit.h>
#import "AppDelegate+OneOnOneView.h"

@implementation AppDelegate (OneOnOneView)
- (void)vr_initWindow {
  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  self.window.backgroundColor = UIColor.whiteColor;

  NEOneOnOneRoomListViewController *vc = [[NEOneOnOneRoomListViewController alloc] init];
  NEUIBackNavigationController *appNav =
      [[NEUIBackNavigationController alloc] initWithRootViewController:vc];

  //  self.tab = [[UITabBarController alloc] init];
  //  self.tab.tabBar.tintColor = [UIColor whiteColor];
  //  self.tab.tabBar.barStyle = UIBarStyleBlack;
  //  self.tab.viewControllers = @[ appNav ];

  self.window.rootViewController = appNav;
  [self.window makeKeyAndVisible];
}

@end
