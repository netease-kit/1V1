// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NERtcCallKit/NECallEngine.h>
#import "NECallEngine+Party.h"
#import "NEOneOnOneCallViewController+RtcCall.h"
#import "NEOneOnOneCallViewController.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneLog.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUIKitMacro.h"
#import "NEOneOnOneUIKitUtils.h"

@implementation NEOneOnOneCallViewController (RtcCall)

- (void)onUserCancel:(NSString *)userID {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onUserCancel:%@", tag, userID]];
  [self endRoom];
  [NEOneOnOneToast showToast:NELocalizedString(@"对方已取消连接")];
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
  [[NSNotificationCenter defaultCenter] postNotificationName:NEOneOnOneCallViewControllerBusy
                                                      object:nil];
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

- (void)onCallConnected:(NECallInfo *)info {
  // 等同于原 onJoinChannel
  [[NEOneOnOneKit getInstance]
      reportRtcRoom:info.rtcInfo.channelId
           callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
             if (code != 0) {
               [NEOneOnOneLog infoLog:tag
                                 desc:[NSString stringWithFormat:@"%@:reportRtcRoom:%llu,error:%@",
                                                                 tag, info.rtcInfo.channelId, msg]];
             }
           }];
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onJoinChannel", tag]];
  [self userAccept];
  [self userEnterVideo];
}
- (void)onCallEnd:(NECallEndInfo *)info {
  switch (info.reasonCode) {
    case TerminalCodeNormal:
    case TerminalBeHuangUp:
    case TerminalCodeKicked:
    case TerminalCodeJoinRtcError:
      [self onCallEnd];
      break;
    case TerminalHuangUp:
      // 用户主动挂断，需要发送通知。可能不在当前页面挂断，所以需要统一接受这个回调处理
      [self userHangUp];
      break;
    case TerminalCalleeCancel:
      [self onUserCancel:self.callParam.remoteUserAccid];
      break;
    case TerminalCallerRejcted:
      [self onUserReject:self.callParam.remoteUserAccid];
      break;
    case TerminalCodeBusy:
      [self onUserBusy:self.callParam.remoteUserAccid];
      break;
    case TerminalCodeTimeOut:
      [self onCallingTimeOut];
      break;
    case TerminalRtcDisconnected:
      // 本端断网
      [self onDisconnect:nil];
      break;
    case TerminalUserRtcDisconnected:
      // 对端断网
      [self onUserDisconnect:self.callParam.remoteUserAccid];
      break;

    case TerminalUserRtcLeave:
      [self onUserLeave:self.callParam.remoteUserAccid];
      break;
    default:
      [self onError:nil];
      break;
  }
}

- (void)userHangUp {
  // 正常挂断回调会回来，断网情况下收到不及时，所以需要业务层提前处理;
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:userHangUp", tag]];
  [self endRoom];
}
- (void)onCallEnd {
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:onCallEnd", tag]];
  NSLog(@"delegate - onCallEnd");
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
  if (self.isSmallWindow == YES) {
    self.coverView.hidden = !muted;
  }
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
  [[NECallEngine sharedInstance] hangup:[[NEHangupParam alloc] init] completion:nil];
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

///// 用户加入需要走这个回调
//- (void)onNERtcEngineUserDidJoinWithUserID:(uint64_t)userID userName:(NSString *)userName {
//  self.remoteUserId = userID;
//}
@end
