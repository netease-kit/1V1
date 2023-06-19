// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

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

static NERtcChannelProfileType RTCConfig_channelProfile = kNERtcChannelProfileLiveBroadcasting;
static NERtcAudioProfileType RTCConfig_audioProfile = kNERtcAudioProfileStandard;
static NERtcAudioScenarioType RTCConfig_scenario = kNERtcAudioScenarioSpeech;
static NERtcVideoFrameRate RTCConfig_videoFrame_oversea = kNERtcVideoFrameRateFps15;
static NERtcVideoFrameRate RTCConfig_videoFrame_online = kNERtcVideoFrameRateFps24;
static int RTCConfig_videoWidth = 640;
static int RTCConfig_videoHeight = 480;

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
  [NERtcEngine.sharedEngine setChannelProfile:RTCConfig_channelProfile];
  [NERtcEngine.sharedEngine setAudioProfile:RTCConfig_audioProfile scenario:RTCConfig_scenario];
  NERtcVideoEncodeConfiguration *videoConfig = [[NERtcVideoEncodeConfiguration alloc] init];
  if ([NEOneOnOneKit getInstance].isOversea) {
    videoConfig.frameRate = RTCConfig_videoFrame_oversea;
  } else {
    videoConfig.frameRate = RTCConfig_videoFrame_online;
  }
  videoConfig.width = RTCConfig_videoWidth;
  videoConfig.height = RTCConfig_videoHeight;
  [NERtcEngine.sharedEngine setLocalVideoConfig:videoConfig];

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
