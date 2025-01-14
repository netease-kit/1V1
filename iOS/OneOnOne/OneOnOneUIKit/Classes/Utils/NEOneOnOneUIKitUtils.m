// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUIKitUtils.h"

static UIWindow *subWindow;
@implementation NEOneOnOneUIKitUtils

// 获取当前时间戳
+ (NSString *)currentTimeStr {
  NSDate *date = [NSDate dateWithTimeIntervalSinceNow:0];  // 获取当前时间0秒后的时间
  NSTimeInterval time = [date timeIntervalSince1970];      // *1000 是精确到毫秒，不乘就是精确到秒
  NSString *timeString = [NSString stringWithFormat:@"%.0f", time];
  return timeString;
}

/**持续时间*/
+ (long long)getDurationStartTime:(NSString *)startTime endTime:(NSString *)endTime {
  if (startTime && endTime) {
    long long aTime = [endTime longLongValue] - [startTime longLongValue];
    return aTime;
  } else {
    return -1;
  }
}

+ (BOOL)permisionDenied:(AVAuthorizationStatus)AVstatus {
  BOOL hasDenied = NO;
  switch (AVstatus) {
      // 允许状态

    case AVAuthorizationStatusAuthorized:
      NSLog(@"Authorized");
      break;

      // 不允许状态，可以弹出一个alertview提示用户在隐私设置中开启权限

    case AVAuthorizationStatusDenied: {
      hasDenied = YES;
    } break;
      // 未知，第一次申请权限

    case AVAuthorizationStatusNotDetermined:
      NSLog(@"not Determined");
      break;

    // 此应用程序没有被授权访问,可能是家长控制权限
    case AVAuthorizationStatusRestricted:
      NSLog(@"Restricted");
      break;
    default:
      break;
  }

  return hasDenied;
}

+ (void)getMicrophonePermissions:(AVMediaType)meidaType
                        complete:(void (^)(BOOL authorized))completion {
  AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:meidaType];
  if (authStatus == AVAuthorizationStatusNotDetermined) {
    [AVCaptureDevice requestAccessForMediaType:meidaType
                             completionHandler:^(BOOL granted) {
                               if (granted) {
                                 if (completion) {
                                   completion(YES);
                                 }
                               } else {
                                 if (completion) {
                                   completion(NO);
                                 }
                               }
                             }];

  } else if (authStatus == AVAuthorizationStatusAuthorized) {
    if (completion) {
      completion(YES);
    }
  } else {
    if (completion) {
      completion(NO);
    }
  }
}

/// 获取当前视图控制器
+ (UIViewController *)findVisibleViewController {
  UIViewController *currentViewController = [NEOneOnOneUIKitUtils getRootViewController];

  BOOL runLoopFind = YES;
  while (runLoopFind) {
    if (currentViewController.presentedViewController) {
      currentViewController = currentViewController.presentedViewController;
    } else {
      if ([currentViewController isKindOfClass:[UINavigationController class]]) {
        currentViewController =
            ((UINavigationController *)currentViewController).visibleViewController;
      } else if ([currentViewController isKindOfClass:[UITabBarController class]]) {
        currentViewController =
            ((UITabBarController *)currentViewController).selectedViewController;
      } else {
        break;
      }
    }
  }

  return currentViewController;
}

+ (UIViewController *)getRootViewController {
  UIViewController *result = nil;

  UIWindow *window = [[UIApplication sharedApplication] keyWindow];
  if (window.windowLevel != UIWindowLevelNormal) {
    NSArray *windows = [[UIApplication sharedApplication] windows];
    for (UIWindow *temp in windows) {
      if (temp.windowLevel == UIWindowLevelNormal) {
        window = temp;
        break;
      }
    }
  }
  // 取当前展示的控制器
  result = window.rootViewController;
  while (result.presentedViewController) {
    result = result.presentedViewController;
  }
  // 如果为UITabBarController：取选中控制器
  if ([result isKindOfClass:[UITabBarController class]]) {
    result = [(UITabBarController *)result selectedViewController];
  }
  // 如果为UINavigationController：取可视控制器
  if ([result isKindOfClass:[UINavigationController class]]) {
    result = [(UINavigationController *)result visibleViewController];
  }
  return result;
}

@end
