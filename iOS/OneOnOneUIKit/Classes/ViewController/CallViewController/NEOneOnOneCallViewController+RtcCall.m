// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NECallKitPstn/NECallKitPstn.h>
#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NERtcCallKit/NERtcCallKit.h>
#import <libextobjc/extobjc.h>
#import "NEOneOnOneCallViewController+RtcCall.h"
#import "NEOneOnOneCallViewController.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneLog.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUIKitEngine.h"
#import "NEOneOnOneUIKitMacro.h"
#import "NEOneOnOneUIKitUtils.h"
#import "NERtcCallKit+Party.h"

@implementation NEOneOnOneCallViewController (RtcCall)

- (void)onUserCancel:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserCancel:%@", tag, userID]];
  [self endRoom];
  [NEOneOnOneToast showToast:NELocalizedString(@"对方已取消连接")];
}

- (void)onUserAccept:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserAccept:%@", tag, userID]];
  //    [self userAccept];
}

/// 远端加入回调
- (void)onUserEnter:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserEnter:%@", tag, userID]];
  [self userAccept];
  [self userEnterVideo];
}
/// 自己加入成功的回调，通常用来上报、统计等
- (void)onJoinChannel:(NERtcCallKitJoinChannelEvent *)event {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onJoinChannel", tag]];
  [self userAccept];
  [self userEnterVideo];
}

- (void)onUserReject:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserReject:%@", tag, userID]];
  NSLog(@"delegate - onUserReject");
  [self endRoom];
  [NEOneOnOneToast showToast:NELocalizedString(@"对方已拒绝你的请求")];
}
- (void)onUserBusy:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserBusy:%@", tag, userID]];
  NSLog(@"delegate - onUserBusy");
  [self endRoom];
  self.busyBlock();
}

/// PSTN 开始
- (void)onTimeOut {
  // NECallKitPstn bug 需要在超时这里重置一下状态机
  [[NERtcCallKit sharedInstance] changeStatusCalling];

  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onTimeOut", tag]];
  NSLog(@"delegate - onTimeOut");
  if (self.enterStatus == audio_call) {
    /// 音频发起方会走这个代理，而不是onCallingTimeOut代理
    // 邀请方
    self.needShowStringWhenEndRoom = NELocalizedString(@"无法连接对方，请稍后再试");
  }
}

/// RTC通话超时
- (void)onCallingTimeOut {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onCallingTimeOut", tag]];
  NSLog(@"delegate - onCallingTimeOut");
  self.hasEndRoom = @"onTimeOut";

  if (self.enterStatus == audio_invited || self.enterStatus == video_invited) {
    /// 被邀请方
    [NEOneOnOneToast showToast:NELocalizedString(@"超时未接，已结束连接")];
  } else {
    /// 邀请方
    [NEOneOnOneToast showToast:NELocalizedString(@"无法连接对方，请稍后再试")];
  }

  [self endRoom];
}

- (void)pstnDidStart {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:pstnDidStart", tag]];
  NSLog(@"PSTN start");
}

- (void)pstnOnError:(NSError *_Nullable)error {
  [NEOneOnOneLog infoLog:tag
                    desc:[NSString stringWithFormat:@"%@:pstnOnError:%@", tag, error.description]];
  NSLog(@"pstnOnError");
  self.hasEndRoom = @"pstnOnError";
  [NEOneOnOneToast showToast:NELocalizedString(@"无法连接对方，请稍后再试")];
  [self endRoom];
  NSLog(@"PSTN error");
}

- (void)pstnWillStart {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:pstnWillStart", tag]];
  self.isPstnCall = YES;
  self.needShowStringWhenEndRoom = nil;
  NSLog(@"PSTN will start");
}

- (void)onCallEnd {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onCallEnd", tag]];
  NSLog(@"delegate - onCallEnd");
  if (self.enterStatus == audio_call) {
    // 发起方计时
    if (self.startPstnTime.length > 0) {
      NSString *currentTime = [NEOneOnOneUIKitUtils currentTimeStr];
      long long duration = [NEOneOnOneUIKitUtils getDurationStartTime:self.startPstnTime
                                                              endTime:currentTime];
      if (duration > 0) {
        /// 获取本地存储值
        NSMutableDictionary *localDurationDic = [[[NSUserDefaults standardUserDefaults]
            dictionaryForKey:NELocalConnectingDuration] mutableCopy];
        if (localDurationDic == nil) {
          localDurationDic = [NSMutableDictionary dictionary];
        }
        NSString *localUid = [NEOneOnOneKit getInstance].localMember.imAccid;
        NSString *localDuration = @"0";
        if ([localDurationDic.allKeys containsObject:localUid]) {
          localDuration = localDurationDic[localUid];
        }

        long long totalTime = localDuration.longLongValue + duration;

        if (localUid.length > 0 && totalTime > 0) {
          [localDurationDic setValue:[NSString stringWithFormat:@"%lld", totalTime]
                              forKey:localUid];

          [[NSUserDefaults standardUserDefaults] setValue:localDurationDic
                                                   forKey:NELocalConnectingDuration];
        }
      }
    }
  }
  self.startPstnTime = nil;

  if (self.hasEndRoom.length > 0) {
    /// 已经由于其他原因关闭了房间
    self.hasEndRoom = nil;
    return;
  }
  BOOL isAudio = (self.enterStatus == audio_call || self.enterStatus == audio_invited);
  [self endRoom];
  if (isAudio) {
    if (self.needShowStringWhenEndRoom.length > 0) {
      [NEOneOnOneToast showToast:[NSString stringWithFormat:@"%@", self.needShowStringWhenEndRoom]];
      self.needShowStringWhenEndRoom = nil;
    } else {
      [NEOneOnOneToast showToast:NELocalizedString(@"对方结束通话")];
    }
  } else {
    [NEOneOnOneToast showToast:NELocalizedString(@"对方结束视频")];
  }
}
/// 本端断网
- (void)onDisconnect:(NSError *)reason {
  [NEOneOnOneLog
      infoLog:tag
         desc:[NSString stringWithFormat:@"%@:onDisconnect:%@", tag, reason.description]];
  NSLog(@"delegate - onDisconnect");
  [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，通话已结束")];
  [self endRoom];
}

/// 对端断网
- (void)onUserDisconnect:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag
                    desc:[NSString stringWithFormat:@"%@:onUserDisconnect:%@", tag, userID]];
  NSLog(@"delegate - onUserDisconnect");
  [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，通话已结束")];
  [self endRoom];
}

- (void)onVideoMuted:(BOOL)muted userID:(NSString *)userID {
  [self onVideoMute:muted userID:userID];
}
- (void)onUserLeave:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserLeave:%@", tag, userID]];
  NSLog(@"delegate - onUserLeave");
  BOOL isAudio = (self.enterStatus == audio_call || self.enterStatus == audio_invited);
  if (isAudio) {
    [NEOneOnOneToast showToast:NELocalizedString(@"对方结束通话")];
  } else {
    [NEOneOnOneToast showToast:NELocalizedString(@"对方结束视频")];
  }
  [[NERtcCallKit sharedInstance] hangup:nil];
  [self endRoom];
}

// Pstn接通
- (void)onDirectCallAccept {
  self.pstnAccept = YES;
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onDirectCallAccept", tag]];
  self.startPstnTime = [NEOneOnOneUIKitUtils currentTimeStr];
  [self userAccept];
}

/// PSTN 断开连接
- (void)onDirectCallDisconnectWithError:(NSError *)error {
  [NEOneOnOneLog infoLog:tag
                    desc:[NSString stringWithFormat:@"%@:onDirectCallDisconnectWithError:%@", tag,
                                                    error.description]];
  self.hasEndRoom = @"onDirectCallDisconnectWithError";
  NSLog(@"delegate - onDirectCallDisconnectWithError");
  [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，通话已结束")];
  [self endRoom];
  [NEOneOnOneLog
      infoLog:tag
         desc:[NSString stringWithFormat:@"onDirectCallDisconnectWithError:code = %ld,detail = %@",
                                         (long)error.code, error.description]];
}

/// PSTN 挂断电话
- (void)onDirectCallHangupWithReason:(int)reason
                               error:(NSError *)error
                   isCallEstablished:(BOOL)isCallEstablished {
  [NEOneOnOneLog
      infoLog:tag
         desc:[NSString stringWithFormat:@"%@:onDirectCallHangupWithReason:%d", tag, reason]];
  self.hasEndRoom = @"onDirectCallHangupWithReason";
  if (reason == 16) {
    /// 拒绝
    if (self.pstnAccept) {
      [NEOneOnOneToast showToast:NELocalizedString(@"对方结束通话")];
    } else {
      [NEOneOnOneToast showToast:NELocalizedString(@"对方已拒绝你的请求")];
    }

  } else if (reason == 1000) {
    /// 主动挂断
  }
  [NEOneOnOneLog
      infoLog:tag
         desc:[NSString stringWithFormat:
                            @"onDirectCallHangupWithReason:reason:%d, code = %ld,detail = %@",
                            reason, (long)error.code, error.description]];
  [self endRoom];
}

- (void)onError:(NSError *)error {
  [NEOneOnOneLog infoLog:tag
                    desc:[NSString stringWithFormat:@"%@:onError:%@", tag, error.description]];
  NSLog(@"delegate - onError");
  [self endRoom];
  [NEOneOnOneLog
      infoLog:tag
         desc:[NSString stringWithFormat:@"onDirectCallDisconnectWithError:code = %ld,detail = %@",
                                         (long)error.code, error.description]];
}

/// 用户加入需要走这个回调
- (void)onNERtcEngineUserDidJoinWithUserID:(uint64_t)userID userName:(NSString *)userName {
  self.remoteUserId = userID;
}
@end
