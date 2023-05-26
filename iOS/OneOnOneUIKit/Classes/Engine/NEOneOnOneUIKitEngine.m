// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUIKitEngine.h"
#import <NECallKitPstn/NECallKitPstn.h>
#import <NEOneOnOneKit/NEOneOnOneLog.h>
#import <NERtcCallKit/NERtcCallKit.h>
#import "NEOneOnOneCallViewController.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUIKitMacro.h"
#import "NEOneOnOneUIKitUtils.h"
#import "NERtcCallKit+Party.h"
static NSString *engineTag = @"NEOneOnOneUIKitEngine";

@interface NEOneOnOneUIKitEngine () <NERtcLinkEngineDelegate,
                                     NECallKitPstnDelegate,
                                     NERtcCallKitDelegate,
                                     NEOneOnOneListener>
// 通话中视图控制器

//@property(nonatomic, weak) NEOneOnOneCallViewController *target;

@end

@implementation NEOneOnOneUIKitEngine

+ (NEOneOnOneUIKitEngine *)sharedInstance {
  static NEOneOnOneUIKitEngine *instance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    instance = [[NEOneOnOneUIKitEngine alloc] init];
    [NEOneOnOneKit getInstance].interceptor = ^BOOL {
      if (instance.interceptor) {
        BOOL isBusy = instance.interceptor();
        if (isBusy) {
          // #pragma mark TODO
          //         /// 需要确认是否要迁移到 all In One 中
          //         /// 1v1的业务逻辑
          //         /// all in one 才会存在
          //
          //         [NEOneOnOneUIKitUtils
          //             presentAlertViewController:[self getRootViewController]
          //                                 titile:NELocalizedString(@"对方正在忙碌中\n请稍后再试")
          //                            cancelTitle:@""
          //                           confirmTitle:NELocalizedString(@"确定")
          //                        confirmComplete:nil];
        }
        return isBusy;
      }
      return false;
    };
    [[NEOneOnOneKit getInstance] addOneOnOneListener:instance];
  });
  return instance;
}

- (void)addObserve {
  [[NERtcCallKit sharedInstance] addDelegate:self];
}

- (void)removeObserve {
  [[NERtcCallKit sharedInstance] removeDelegate:self];
}

#pragma mark utilMehod
/// 获取当前视图控制器
- (UIViewController *)findVisibleViewController {
  UIViewController *currentViewController = [self getRootViewController];

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

//- (UIViewController *)getRootViewController {
//  id<UIApplicationDelegate> delegate = [[UIApplication sharedApplication] delegate];
//  if (delegate && [delegate respondsToSelector:@selector(window)]) {
//    UIWindow *window = [delegate window];
//    return window.rootViewController;
//  }
//  return [UIApplication sharedApplication].keyWindow.rootViewController;
//}

- (UIViewController *)getRootViewController {
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

- (void)enterCallViewController:(NEEnterStatus)enterType
                     attachment:(NSString *)attachment
                        invitor:(NSString *)invitor {
  UIViewController *currentViewController = [self findVisibleViewController];
  NEOneOnOneCallViewController *controller = [[NEOneOnOneCallViewController alloc] init];
  /// 被叫..不需要设置PSTN
  controller.needPstnCall = NO;
  [[NERtcCallKit sharedInstance] addDelegate:controller];
  switch (enterType) {
    case audio_invited:
    case video_invited:
      if (attachment.length > 0) {
        NSDictionary *attachmentDic = [self dictionaryWithJsonString:attachment];
        NSString *nickName = attachmentDic[CALLER_USER_NAME];
        NSString *avatar = attachmentDic[CALLER_USER_AVATAR];
        NSString *mobile = attachmentDic[CALLER_USER_MOBILE];

        //                [[NEOneOnOneKit getInstance] getAccountInfo:mobile callback:^(NSInteger
        //                code, NSString * _Nullable msg, NEOneOnOneAccountInfo * _Nullable obj) {
        //                                    if(code != 0){
        //                                        //获取对象信息失败
        //                                    }else{
        //
        //                                    }
        //                }];

        NEOneOnOneOnlineUser *onlineUser = [[NEOneOnOneOnlineUser alloc] init];
        onlineUser.userName = nickName;
        onlineUser.icon = avatar;
        onlineUser.mobile = mobile;
        onlineUser.userUuid = invitor;
        controller.remoteUser = onlineUser;

        controller.enterStatus = enterType;
        dispatch_async(dispatch_get_main_queue(), ^{
          controller.modalPresentationStyle = UIModalPresentationOverFullScreen;
          [currentViewController presentViewController:controller animated:YES completion:nil];
        });
      }
      break;
    default:
      break;
  }
}

#pragma mark NERtcCallKitDelegate
//- (void)onInvited:(NSString *)invitor
//          userIDs:(NSArray<NSString *> *)userIDs
//      isFromGroup:(BOOL)isFromGroup
//          groupID:(NSString *)groupID
//             type:(NERtcCallType)type
//       attachment:(NSString *)attachment {
//  NSLog(@"receive invite = %@ ; userIDs = %@", invitor, userIDs);
//        if (type == NERtcCallTypeAudio) {
//          [self enterCallViewController:audio_invited attachment:attachment invitor:invitor];
//        } else {
//          [self enterCallViewController:video_invited attachment:attachment invitor:invitor];
//        }
//}

- (void)onOneOnOneInvited:(NSString *)invitor
                  userIDs:(NSArray<NSString *> *)userIDs
              isFromGroup:(BOOL)isFromGroup
                  groupID:(NSString *)groupID
                     type:(enum NEOneOnOneRtcCallType)type
               attachment:(NSString *)attachment {
  [NEOneOnOneLog infoLog:engineTag
                    desc:[NSString stringWithFormat:@"收到Invite信息 %@,%@,%ld,%@,", invitor,
                                                    userIDs, (long)type, attachment]];
  NSLog(@"收到Invite信息 %@,%@,%ld,%@,", invitor, userIDs, (long)type, attachment);
  // 如果是黑名单成员，直接拒接
  if ([NIMSDK.sharedSDK.userManager isUserInBlackList:invitor]) {
    [[NERtcCallKit sharedInstance] reject:nil];
    return;
  }
  // 处理异常case，如果上一个target还处于销毁中，那么循环等待吧
  if (type == NEOneOnOneRtcCallTypeAudio) {
    [[NERtcCallKit sharedInstance] setTimeOutSeconds:15];
    [self enterCallViewController:audio_invited attachment:attachment invitor:invitor];
  } else {
    [[NERtcCallKit sharedInstance] setTimeOutSeconds:30];
    [self enterCallViewController:video_invited attachment:attachment invitor:invitor];
  }
}

#pragma mark Util

- (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
  if (jsonString == nil) {
    return nil;
  }

  NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];

  NSError *err;

  NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData

                                                      options:NSJSONReadingMutableContainers

                                                        error:&err];

  if (err) {
    NSLog(@"json解析失败：%@", err);

    return nil;
  }

  return dic;
}

/// 是否在1v1房间中
- (BOOL)isInOneInOne {
  return [[NEOneOnOneKit getInstance] isInOneInOne];
}

/// 状态机处理
- (void)setRTCIdle {
  [[NERtcCallKit sharedInstance] changeStatusIdle];
}
- (void)setRTCCaling {
  [[NERtcCallKit sharedInstance] changeStatusCalling];
}
@end
