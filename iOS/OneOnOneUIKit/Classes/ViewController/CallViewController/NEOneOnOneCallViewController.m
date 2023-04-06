// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneCallViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <Masonry/Masonry.h>
#import <NEOneOnOneKit/NEOneOnOneLog.h>
#import <NERtcCallKit/NERtcCallKit.h>
#import <NEUIKit/NEUIKit.h>
#import <libextobjc/extobjc.h>
#import "NEOneOnOneCallViewController+RtcCall.h"
#import "NEOneOnOneCustomTimer.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneReachability.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUI.h"
#import "NEOneOnOneUIKitUtils.h"

@interface NEOneOnOneCallViewController ()

@property(nonatomic, strong) dispatch_queue_t timeQueue;
@property(nonatomic, strong) NEOneOnOneCustomTimer *timer;
@property(nonatomic, assign) int timerCount;
@property(nonatomic, strong) NSMutableDictionary *timerDict;
@property(nonatomic, strong) NSString *timerIdentifier;
/// 网络监听
@property(nonatomic, strong) NEOneOnOneReachability *reachability;

@end

@implementation NEOneOnOneCallViewController

- (void)viewDidLoad {
  [super viewDidLoad];
  NSError *active_err;
  NSError *setCategory_err;
  //    try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayAndRecord, with:
  //    .duckOthers)
  [AVAudioSession.sharedInstance setCategory:AVAudioSessionCategoryPlayback error:&setCategory_err];
  [AVAudioSession.sharedInstance setActive:YES error:&active_err];
  if (active_err || setCategory_err) {
    [NEOneOnOneLog
        infoLog:tag
           desc:[NSString stringWithFormat:@"%@:setActive:%@,setCategory:%@", tag,
                                           active_err.description, setCategory_err.description]];
  }
  //  if (self.needPstnCall) {
  //      [[NERtcCallKit sharedInstance] removeDelegate:self];
  //      [[NECallKitPstn sharedInstance] addDelegate:self];
  //      [[NECallKitPstn sharedInstance] addPstnDelegate:self];
  //  } else {
  //      [[NECallKitPstn sharedInstance] removeDelegate];
  //      [[NECallKitPstn sharedInstance] removePstnDelegate];
  //      [[NERtcCallKit sharedInstance] addDelegate:self];
  //
  //  }
  /// 页面展示，销毁列表页面提示框
  [[NSNotificationCenter defaultCenter] postNotificationName:NEOneOnOneCallViewControllerAppear
                                                      object:nil];

  [self addNetworkObserver];
  self.isPstnCall = NO;
  self.timerDict = [NSMutableDictionary dictionary];
  self.timeQueue = dispatch_queue_create("com.oneOnOne.timer", DISPATCH_QUEUE_SERIAL);
  //    self.navigationController.navigationBar.hidden = YES;
  self.ne_UINavigationItem.navigationBarHidden = YES;
  [self.view addSubview:self.connectingView];
  [[NEOneOnOneKit getInstance] addOneOnOneListener:self];
  [NERtcCallKit sharedInstance].engineDelegate = self;
  @weakify(self) self.connectingView.itemEvent = ^(Item item) {
    @strongify(self) switch (item) {
      case item_cancel: {
        @weakify(self) if (self.isPstnCall) {
          [[NECallKitPstn sharedInstance] hangupWithCompletion:^(NSError *_Nullable error) {
            @strongify(self)[self endRoom];
          }];
        }
        else {
          [[NERtcCallKit sharedInstance] cancel:^(NSError *_Nullable error) {
            @strongify(self) if (error) {
              [NEOneOnOneLog errorLog:tag desc:error.localizedDescription];
              NSLog(@"cancel - error -- %@", error.description);
              if (error.code == 20016) {
                // 对方已接通的code，所以不能取消
              } else {
                [self endRoom];
              }
            }
            else {
              [self endRoom];
            }
          }];
        }
        NetworkStatus status = [self.reachability currentReachabilityStatus];
        if (status == NotReachable) {
          [self endRoom];
          return;
        }
      }

      break;
      case item_reject: {
        [[NERtcCallKit sharedInstance] reject:^(NSError *_Nullable error) {
          @strongify(self) if (error) {
            NSLog(@"reject - error -- %@", error.description);
          }
          [self endRoom];
        }];
        NetworkStatus status = [self.reachability currentReachabilityStatus];
        if (status == NotReachable) {
          [self endRoom];
          return;
        }
      }

      break;
      case item_accept: {
        __block BOOL hasPermissions = false;
        NetworkStatus status = [self.reachability currentReachabilityStatus];
        if (status == NotReachable) {
          [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
          return;
        }
        dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);

        if (self.enterStatus == audio_invited) {
          [NEOneOnOneUIKitUtils
              getMicrophonePermissions:AVMediaTypeAudio
                              complete:^(BOOL authorized) {
                                if (authorized) {
                                  hasPermissions = YES;
                                  dispatch_semaphore_signal(semaphore);
                                } else {
                                  [NEOneOnOneToast
                                      showToast:NELocalizedString(
                                                    @"麦克风权限已关闭，请开启后重试")];
                                  dispatch_semaphore_signal(semaphore);
                                }
                              }];
        } else if (self.enterStatus == video_invited) {
          [NEOneOnOneUIKitUtils
              getMicrophonePermissions:AVMediaTypeAudio
                              complete:^(BOOL authorized) {
                                if (authorized) {
                                  /// 已获得语音权限或者首次申请已通过
                                  [NEOneOnOneUIKitUtils
                                      getMicrophonePermissions:AVMediaTypeVideo
                                                      complete:^(BOOL authorized) {
                                                        if (authorized) {
                                                          /// 已获得视频权限或者首次申请已通过
                                                          hasPermissions = YES;
                                                          dispatch_semaphore_signal(semaphore);
                                                        } else {
                                                          /// 权限被拒
                                                          [NEOneOnOneToast
                                                              showToast:NELocalizedString(
                                                                            @"摄像头权限已关闭，请"
                                                                            @"开启后重试")];
                                                          dispatch_semaphore_signal(semaphore);
                                                        }
                                                      }];

                                } else {
                                  [NEOneOnOneUIKitUtils
                                      getMicrophonePermissions:AVMediaTypeVideo
                                                      complete:^(BOOL authorized) {
                                                        if (authorized) {
                                                          /// 已获得视频权限或者首次申请已通过
                                                          [NEOneOnOneToast
                                                              showToast:NELocalizedString(
                                                                            @"麦克风权限已关闭，请"
                                                                            @"开启后重试")];
                                                          dispatch_semaphore_signal(semaphore);
                                                        } else {
                                                          /// 权限被拒
                                                          [NEOneOnOneToast
                                                              showToast:NELocalizedString(
                                                                            @"麦克风摄像头权限已关"
                                                                            @"闭，请开启后重试")];
                                                          dispatch_semaphore_signal(semaphore);
                                                        }
                                                      }];
                                }
                              }];
        }
        dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
        if (hasPermissions) {
        } else {
          return;
        }
        NSLog(@"权限判断通过");

        if (self.enterStatus == audio_invited) {
          [self.connectingView updateUI:self.remoteUser.icon
                             remoteName:self.remoteUser.userName
                                 status:audio_call_connecting];
        }

        [[NERtcCallKit sharedInstance] accept:^(NSError *_Nullable error) {
          @strongify(self) if (error) {
            if (error.code == 10404) {
              /// NERTCCallKit 内部问题，后期会优化
              [self endRoom];
            }
            NSLog(@"error -- %@", error.description);
          }
          NSLog(@"本地状态值 --- %lu", (unsigned long)self.enterStatus);
        }];

      }

      break;
      case item_close: {
        if (self.isPstnCall) {
          [[NECallKitPstn sharedInstance] hangupWithCompletion:^(NSError *_Nullable error) {
            BOOL isAudio = (self.enterStatus == audio_call || self.enterStatus == audio_invited);
            if (isAudio) {
              [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
            } else {
              [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
            }
          }];
        } else {
          [[NERtcCallKit sharedInstance] hangup:^(NSError *_Nullable error) {
            if (error) {
              NSLog(@"Pstn hangup - error -- %@", error.description);
            } else {
              BOOL isAudio = (self.enterStatus == audio_call || self.enterStatus == audio_invited);
              if (isAudio) {
                [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
              } else {
                [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
              }
            }
          }];
        }
        [self endRoom];
      }

      break;

      default:
        break;
    }
  };

  self.connectingView.itemExpand = ^(Item item, BOOL close) {
    @strongify(self) switch (item) {
      case item_mic: {
        [[NERtcCallKit sharedInstance] muteLocalAudio:close];
      }

      break;
      case item_speaker: {
        [NERtcEngine.sharedEngine adjustUserPlaybackSignalVolume:close ? 0 : 100
                                                       forUserID:self.remoteUserId];
        //        [[NERtcCallKit sharedInstance] setAudioMute:close
        //                                            forUser:self.remoteUser.accountId
        //                                              error:nil];
      }

      break;

      default:
        break;
    }
  };

  [self.connectingView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.top.right.bottom.equalTo(self.view);
  }];

  [self.view addSubview:self.videoConnectedView];
  [self.videoConnectedView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.top.right.bottom.equalTo(self.view);
  }];
  self.videoConnectedView.hidden = YES;

  self.videoConnectedView.itemExpand = ^(Item item, BOOL close) {
    @strongify(self) switch (item) {
      case item_close: {
        if (self.timer) {
          [self.timer invalidate];
        }
        [[NERtcCallKit sharedInstance] hangup:^(NSError *_Nullable error) {
          if (error) {
            NSLog(@"Pstn hangup - error -- %@", error.description);
          } else {
            BOOL isAudio = (self.enterStatus == audio_call || self.enterStatus == audio_invited);
            if (isAudio) {
              [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
            } else {
              [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
            }
            [self endRoom];
          }
        }];
        [self endRoom];
      } break;

      case item_mic: {
        [[NERtcCallKit sharedInstance] muteLocalAudio:close];
      }

      break;
      case item_speaker: {
        //        [[NERtcCallKit sharedInstance] setAudioMute:close
        //                                            forUser:self.remoteUser.accountId
        //                                              error:nil];
        [NERtcEngine.sharedEngine adjustUserPlaybackSignalVolume:close ? 0 : 100
                                                       forUserID:self.remoteUserId];
      } break;
      case item_switch_camera: {
        [[NERtcCallKit sharedInstance] switchCamera];
      }

      break;

      case item_video_close: {
        //                [[NERtcCallKit sharedInstance] enableLocalVideo:!close];
        [[NERtcCallKit sharedInstance] muteLocalVideo:close];
      }

      break;
      case item_video_change: {
        [self refreshVideoView:close];
      } break;
      default:
        break;
    }
  };
  [self enterViewController];

  // 禁止返回
  id traget = self.navigationController.interactivePopGestureRecognizer.delegate;
  UIPanGestureRecognizer *pan = [[UIPanGestureRecognizer alloc] initWithTarget:traget action:nil];
  [self.view addGestureRecognizer:pan];
}

- (void)enterViewController {
  // 根据参数处理内容
  switch (self.enterStatus) {
    case audio_call: {
      [self.connectingView updateUI:self.remoteUser.icon
                         remoteName:self.remoteUser.userName
                             status:audio_call_start];
    } break;
    case video_call: {
      [self.connectingView updateUI:self.remoteUser.icon
                         remoteName:self.remoteUser.userName
                             status:video_call_start];
    } break;
    case audio_invited: {
      [self.connectingView updateUI:self.remoteUser.icon
                         remoteName:self.remoteUser.userName
                             status:audio_invide_start];
    } break;

    case video_invited: {
      [self.connectingView updateUI:self.remoteUser.icon
                         remoteName:self.remoteUser.userName
                             status:video_invite_start];
    } break;
    default:
      break;
  }
}

- (NEOneOnOneConnectingView *)connectingView {
  if (!_connectingView) {
    _connectingView = [[NEOneOnOneConnectingView alloc] init];
  }
  return _connectingView;
}

- (NEOneOnOneVideoConnectedView *)videoConnectedView {
  if (!_videoConnectedView) {
    _videoConnectedView = [[NEOneOnOneVideoConnectedView alloc] init];
  }
  return _videoConnectedView;
}
- (void)userAccept {
  @weakify(self) NSLog(@"用户同意");
  if (self.enterStatus == audio_call || self.enterStatus == audio_invited) {
    dispatch_async(dispatch_get_main_queue(), ^{
      @strongify(self) self.connectingView.hidden = NO;
      self.videoConnectedView.hidden = YES;
      [self.connectingView updateUI:self.remoteUser.icon
                         remoteName:self.remoteUser.userName
                             status:audio_call_connecting];
    });
    //    dispatch_async(self.timeQueue, ^() {
    //      @strongify(self) if (self && !self.timer) {
    //        self.timerCount = 0;
    //        self.timer = [[NEOneOnOneCustomTimer alloc] initWithTimeInterval:1
    //                                                                  target:self
    //                                                                selector:@selector(timeUp)
    //                                                                 repeats:YES];
    //        [[NSRunLoop currentRunLoop] addTimer:self.timer.timer forMode:NSRunLoopCommonModes];
    //        [[NSRunLoop currentRunLoop] run];
    //      }
    //    });
    self.timerIdentifier = [self
            schedleTask:^{
              @strongify(self)[self timeUp];
            }
               interval:1
                 repeat:YES
                  async:YES
        reuseIdentifier:@"NEOneOnOneCallViewController"];

  } else if (self.enterStatus == video_call || self.enterStatus == video_invited) {
    [self refreshVideoUI];
  }
}

- (void)userEnterVideo {
  if (self.enterStatus == video_call || self.enterStatus == video_invited) {
    [self refreshVideoView:NO];
  }
}

- (void)refreshVideoUI {
  @weakify(self) dispatch_async(dispatch_get_main_queue(), ^{
    @strongify(self) self.connectingView.hidden = YES;
    self.videoConnectedView.hidden = NO;
    [self.videoConnectedView updateUI:self.remoteUser.icon remoteName:self.remoteUser.userName];
  });
  //   dispatch_async(self.timeQueue, ^() {
  //    @strongify(self) if (self && !self.timer) {
  //      self.timerCount = 0;
  //      self.timer = [[NEOneOnOneCustomTimer alloc] initWithTimeInterval:1
  //                                                                target:self
  //                                                              selector:@selector(timeUp)
  //                                                               repeats:YES];
  //      [[NSRunLoop currentRunLoop] addTimer:self.timer.timer forMode:NSRunLoopCommonModes];
  //      [[NSRunLoop currentRunLoop] run];
  //    }
  //  });
  self.timerIdentifier = [self
          schedleTask:^{
            @strongify(self)[self timeUp];
          }
             interval:1
               repeat:YES
                async:YES
      reuseIdentifier:@"NEOneOnOneCallViewController"];
}

- (void)refreshVideoView:(BOOL)convert {
  dispatch_async(dispatch_get_main_queue(), ^{
    [[NERtcCallKit sharedInstance] setupLocalView:convert ? self.videoConnectedView.remoteVideoView
                                                          : self.videoConnectedView.localVideoView];
    NSLog(@"self.status == NERtcCallStatusInCall enableLocalVideo:YES");
    [[NERtcCallKit sharedInstance] setupRemoteView:convert ? self.videoConnectedView.localVideoView
                                                           : self.videoConnectedView.remoteVideoView
                                           forUser:self.remoteUser.accountId];
  });
}

/// 摄像头变化回调
- (void)onVideoMute:(BOOL)mute userID:(NSString *)userID {
  if ([self isLocalUser:userID]) {
    // 本端用户
    [self.videoConnectedView showLocalBlackView:mute];
  } else {
    [self.videoConnectedView showRemoteBlackView:mute];
  }
}

- (void)timeUp {
  self.timerCount += 1;
  if (self.enterStatus == audio_call || self.enterStatus == audio_invited) {
    [self.connectingView updateTimer:[self getMMSSFromSS:self.timerCount]];
  } else if (self.enterStatus == video_call || self.enterStatus == video_invited) {
    [self.videoConnectedView updateTimer:[self getMMSSFromSS:self.timerCount]];
  }
}

// 传入 秒  得到 xx:xx:xx
- (NSString *)getMMSSFromSS:(int)totalTime {
  NSInteger seconds = totalTime;

  // format of hour
  NSString *str_hour = [NSString stringWithFormat:@"%02ld", seconds / 3600];
  // format of minute
  NSString *str_minute = [NSString stringWithFormat:@"%02ld", (seconds % 3600) / 60];
  // format of second
  NSString *str_second = [NSString stringWithFormat:@"%02ld", seconds % 60];
  // format of time
  NSString *format_time;
  if (str_hour.intValue > 0) {
    format_time = [NSString stringWithFormat:@"%@:%@:%@", str_hour, str_minute, str_second];
  } else {
    format_time = [NSString stringWithFormat:@"%@:%@", str_minute, str_second];
  }

  return format_time;
}

- (void)endRoom {
  NSLog(@"endRoom - 控制器释放");
  //  if (self.timer) {
  //    [self.timer invalidate];
  //  }
  [self cancelTimer:self.timerIdentifier];
  [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%@:endRoom", tag]];
  NSLog(@"定时器----%@", self.timer);
  dispatch_async(dispatch_get_main_queue(), ^{
    [self dismissViewControllerAnimated:YES completion:nil];
    //    [self.navigationController popViewControllerAnimated:YES];
  });
}

- (void)dealloc {
  NSLog(@"dealloc - 控制器释放");
  /// 恢复其他音乐播放
  [[AVAudioSession sharedInstance] setActive:NO
                                 withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
                                       error:nil];

  [[NEOneOnOneKit getInstance] removeOneOnOneListener:self];
  [self destroyNetworkObserver];
}

- (BOOL)isLocalUser:(NSString *)uid {
  if ([[NEOneOnOneKit getInstance].localMember.imAccid isEqualToString:uid]) {
    return YES;
  }
  return NO;
}
#pragma mark OneOnOneListener

- (void)onReceiveCustomMessageWithMessage:(NEOneOnOneCustomMessage *)message {
  if (message.type == 400 || message.type == 401) {
    if (message.data.audio) {
      NSString *uid = [NSString stringWithFormat:@"%lld", message.data.audio.uid];
      if (uid.length > 0 && [uid intValue] > 0) {
        if ([self isLocalUser:uid]) {
          [NEOneOnOneToast showToast:NELocalizedString(@"您的言语涉及敏感内容，请文明用语哦~")];
        }
      }
    }
    if (message.data.video) {
      NSString *uid = [NSString stringWithFormat:@"%lld", message.data.video.uid];
      if (uid.length > 0 && [uid intValue] > 0) {
        // 判断是否为本端用户
        if ([self isLocalUser:uid]) {
          // 屏蔽本端画面
          [self.videoConnectedView showSmallEffectView:YES];
          [NEOneOnOneToast showToast:@"您的画面涉及敏感内容，请文明互动~\n画面已自动屏蔽"];
        } else {
          // 屏蔽对端画面
          [self.videoConnectedView showLargeEffectView:YES];
          [NEOneOnOneToast showToast:@"对方画面涉及敏感内容,\n画面已自动屏蔽"];
        }
      }
    }
  } else if (message.type == 3000) {
    [self.videoConnectedView showSmallEffectView:NO];
    [self.videoConnectedView showLargeEffectView:NO];
  }
}

/// GCD 定时器
- (NSString *)schedleTask:(void (^)(void))task
                 interval:(NSTimeInterval)interval
                   repeat:(BOOL)repeat
                    async:(BOOL)async
          reuseIdentifier:(NSString *)identifier {
  dispatch_queue_t queue = async ? self.timeQueue : dispatch_get_main_queue();
  // 穿件定时器
  dispatch_source_t timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, queue);
  // 开始时间
  dispatch_time_t start = dispatch_time(DISPATCH_TIME_NOW, 0 * NSEC_PER_SEC);
  // 设置各种时间
  dispatch_source_set_timer(timer, start, interval * NSEC_PER_SEC, 0);
  // 设置回调
  dispatch_source_set_event_handler(timer, ^{
    task();
    if (!repeat) {
      [self cancelTimer:identifier];
    }
  });
  // 启动定时器
  dispatch_resume(timer);
  self.timerDict[identifier] = timer;
  return identifier;
}
- (void)cancelTimer:(NSString *)identifier {
  if (identifier) {
    @try {
      if ([self.timerDict.allKeys containsObject:identifier]) {
        dispatch_source_cancel(self.timerDict[identifier]);
        [self.timerDict removeObjectForKey:identifier];
      }
    } @catch (NSException *exception) {
    } @finally {
    }
  }
}

- (void)addNetworkObserver {
  [self.reachability startNotifier];
  [NSNotificationCenter.defaultCenter addObserver:self
                                         selector:@selector(networkStatusChange)
                                             name:kOneOnOneReachabilityChangedNotification
                                           object:nil];
}
- (void)destroyNetworkObserver {
  [self.reachability stopNotifier];
  [NSNotificationCenter.defaultCenter removeObserver:self];
}
- (void)networkStatusChange {
  // 无网络
  if ([self.reachability currentReachabilityStatus] != NotReachable) {
  } else {
  }
}

- (NEOneOnOneReachability *)reachability {
  if (!_reachability) {
    _reachability = [NEOneOnOneReachability reachabilityForInternetConnection];
  }
  return _reachability;
}

@end
