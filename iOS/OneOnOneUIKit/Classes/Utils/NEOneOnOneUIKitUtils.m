// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUIKitUtils.h"

static UIWindow *subWindow;
@implementation NEOneOnOneUIKitUtils

// 获取当前时间戳
+ (NSString *)currentTimeStr {
  NSDate *date = [NSDate dateWithTimeIntervalSinceNow:0];  // 获取当前时间0秒后的时间
  NSTimeInterval time = [date timeIntervalSince1970];  // *1000 是精确到毫秒，不乘就是精确到秒
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

// 提示框
+ (void)presentAlertViewController:(UIViewController *)target
                            titile:(NSString *)title
                       cancelTitle:(NSString *)cancelTitle
                      confirmTitle:(NSString *)confirmTitle
                   confirmComplete:(void (^)(void))complete {
  UIAlertController *alerVC =
      [UIAlertController alertControllerWithTitle:@""
                                          message:title
                                   preferredStyle:UIAlertControllerStyleAlert];

  if (cancelTitle.length > 0) {
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil)
                                                           style:UIAlertActionStyleCancel
                                                         handler:^(UIAlertAction *_Nonnull action){
                                                         }];
    [alerVC addAction:cancelAction];
  }
  if (confirmTitle.length > 0) {
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"确认", nil)
                                                       style:UIAlertActionStyleDefault
                                                     handler:^(UIAlertAction *_Nonnull action) {
                                                       [subWindow resignKeyWindow];
                                                       subWindow = nil;
                                                       [target.view.window makeKeyAndVisible];
                                                       if (complete) {
                                                         complete();
                                                       }
                                                     }];

    [alerVC addAction:okAction];
  }
  dispatch_async(dispatch_get_main_queue(), ^{
      {
//        static dispatch_once_t onceToken;
//        dispatch_once(&onceToken, ^{
            subWindow = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
            subWindow.rootViewController = [[UIViewController alloc] init];
            subWindow.windowLevel = UIWindowLevelNormal;
            [subWindow makeKeyAndVisible];;
//        });
          [subWindow.rootViewController presentViewController: alerVC
                                                        animated:YES
                                                      completion:nil];
}
});
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

@end
