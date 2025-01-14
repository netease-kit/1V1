// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneCallViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <Masonry/Masonry.h>
#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NEOneOnOneKit/NEOneOnOneLog.h>
#import <NEOneOnOneUIKit/NEOneOnOneUIKit-Swift.h>
#import <NERtcCallKit/NECallEngine.h>
#import <NEUIKit/NEUIKit.h>
#import "NECallEngine+Party.h"
#import "NEOneOnOneCallViewController+RtcCall.h"
#import "NEOneOnOneCustomTimer.h"
#import "NEOneOnOneGiftView.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneReachability.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUI.h"
#import "NEOneOnOneUIKitUtils.h"
@interface NEOneOnOneCallViewController () <NEOneOnOneGiftViewDelegate,
                                            AVCaptureVideoDataOutputSampleBufferDelegate,
                                            NEOneOnOneAuthListener,
                                            NEOneOnOnePlayerProtocol>

@property(nonatomic, strong) dispatch_queue_t timeQueue;
@property(nonatomic, strong) NEOneOnOneCustomTimer *timer;
@property(nonatomic, assign) int timerCount;
@property(nonatomic, strong) NSMutableDictionary *timerDict;
@property(nonatomic, strong) NSString *timerIdentifier;
/// 网络监听
@property(nonatomic, strong) NEOneOnOneReachability *reachability;
@property(nonatomic, strong) NEOneOnOneGiftView *giftAnimation;  // 礼物动画
@property(nonatomic, strong) AVCaptureSession *captureSession;
/// 虚拟房间播放视频相关的
//@property(nonatomic, strong) AVPlayer *player;
@property(nonatomic, strong) AVCaptureDeviceInput *input;
@property(nonatomic, strong) AVAudioSessionCategory originalCategory;
// 防重点
@property(nonatomic, assign) BOOL isEntering;

@property(nonatomic, strong) NEOneOnOneConnectingView *connectingView;
@property(nonatomic, strong) NEOneOnOneVideoConnectedView *videoConnectedView;
@property(nonatomic, strong) UIButton *giftButton;
// 小窗按钮
@property(nonatomic, strong) UIButton *smallWindowButton;
// 是否进行了大小窗切换
@property(nonatomic, assign) BOOL videoConverted;
// 远端是否屏蔽了视频
@property(nonatomic, assign) BOOL remoteVideoMute;

/////音频播放地址
//@property(nonatomic,strong) NSString *audioPlayUrl;
/////视频播放地址
//@property(nonatomic,strong) NSString *videoPlayUrl;
/////是否为虚拟房间，callType为1 是虚拟房间
//@property(nonatomic,assign) int callType;
@end

@implementation NEOneOnOneCallViewController

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(appDidEnterBackground)
                                               name:UIApplicationDidEnterBackgroundNotification
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(appWillEnterForeground)
                                               name:UIApplicationWillEnterForegroundNotification
                                             object:nil];
  [self.navigationController setNavigationBarHidden:YES animated:YES];
}

- (void)viewWillDisappear:(BOOL)animated {
  [super viewWillDisappear:animated];

  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:UIApplicationDidEnterBackgroundNotification
                                                object:nil];
  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:UIApplicationWillEnterForegroundNotification
                                                object:nil];
}

- (void)appDidEnterBackground {
  // 应用进入后台时的处理逻辑
}

- (void)appWillEnterForeground {
  // 应用即将进入前台时的处理逻辑
  [NEOneOnOnePlayerUtil.getInstance playerPlay];
}

- (void)viewDidLoad {
  [super viewDidLoad];
  // 数据解析
  [self extraParamsDataAnalysis];
  self.view.backgroundColor = [UIColor blackColor];
  [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
  NSError *active_err;
  NSError *setCategory_err;
  //    try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayAndRecord, with:
  //    .duckOthers)
  if ([self isVirtualRoom]) {
    [NEOneOnOnePlayerUtil.getInstance addPlayerListener:self];
    // 虚拟房间
    // 设置category 是因为静音模式下，需要播放出声音
    // 另外虚拟房间才会设置，因为设置category导致在后台对端接听后无声音的问题；虚拟房间进行设置，销毁进行恢复
    [NEOneOnOnePlayerUtil.getInstance setOriginalCategray:AVAudioSession.sharedInstance.category];
  } else {
    [[NECallEngine sharedInstance] addCallDelegate:self];
  }
  [AVAudioSession.sharedInstance setActive:YES error:&active_err];
  if (active_err || setCategory_err) {
    [NEOneOnOneLog
        infoLog:tag
           desc:[NSString stringWithFormat:@"%@:setActive:%@,setCategory:%@", tag,
                                           active_err.description, setCategory_err.description]];
  }
  /// 页面展示，销毁列表页面提示框
  [[NSNotificationCenter defaultCenter] postNotificationName:NEOneOnOneCallViewControllerAppear
                                                      object:nil];
  [[NSNotificationCenter defaultCenter] postNotificationName:@"kCallKitShowNoti" object:nil];

  [self addNetworkObserver];
  self.timerDict = [NSMutableDictionary dictionary];
  self.timeQueue = dispatch_queue_create("com.oneOnOne.timer", DISPATCH_QUEUE_SERIAL);
  [self.view addSubview:self.connectingView];
  [[NEOneOnOneKit getInstance] addOneOnOneListener:self];
  [[NEOneOnOneKit getInstance] addAuthListener:self];
  [NECallEngine sharedInstance].engineDelegate = self;
  __weak typeof(self) weakSelf = self;
  self.connectingView.itemEvent = ^(Item item) {
    __strong typeof(weakSelf) self = weakSelf;
    switch (item) {
      case item_cancel: {
        if ([self isVirtualRoom]) {
          [self endRoom];
          return;
        }
        NEHangupParam *param = [[NEHangupParam alloc] init];
        [[NECallEngine sharedInstance] hangup:param
                                   completion:^(NSError *_Nullable error) {
                                     __strong typeof(weakSelf) self = weakSelf;
                                     if (error) {
                                       [NEOneOnOneLog errorLog:tag desc:error.localizedDescription];
                                       NSLog(@"cancel - error -- %@", error.description);
                                       if (error.code == 20016) {
                                         // 对方已接通的code，所以不能取消
                                       } else {
                                         [self endRoom];
                                       }
                                     } else {
                                       [self endRoom];
                                     }
                                   }];

        NetworkStatus status = [self.reachability currentReachabilityStatus];
        if (status == NotReachable) {
          [self endRoom];
          return;
        }
      }

      break;
      case item_reject: {
        self.hasEndRoom = @"end";
        NEHangupParam *param = [[NEHangupParam alloc] init];
        [[NECallEngine sharedInstance] hangup:param
                                   completion:^(NSError *_Nullable error) {
                                     if (error) {
                                       NSLog(@"reject - error -- %@", error.description);
                                     }
                                     __strong typeof(weakSelf) self = weakSelf;
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
        if (self.isEntering) {
          return;
        }
        self.isEntering = YES;

        if (self.enterStatus == audio_invited) {
          [self.connectingView updateUI:self.callParam.remoteAvatar
                             remoteName:self.callParam.remoteShowName
                                 status:audio_call_connecting];
        }

        NEHangupParam *param = [[NEHangupParam alloc] init];
        [[NECallEngine sharedInstance]
            accept:^(NSError *_Nullable error, NECallInfo *_Nullable callInfo) {
              __strong typeof(weakSelf) self = weakSelf;
              if (error) {
                // 避免其他error未及时返回，导致页面接听按钮无效果
                self.isEntering = NO;
                [self endRoom];
                [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
                NSLog(@"error -- %@", error.description);
              }
              NSLog(@"本地状态值 --- %lu", (unsigned long)self.enterStatus);
            }];
      }

      break;
      case item_close: {
        if ([self isVirtualRoom]) {
          BOOL isAudio = (self.enterStatus == audio_call);
          if (isAudio) {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
          } else {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
          }
          [self endRoom];
          return;
        }

        self.hasEndRoom = @"end";
        NEHangupParam *param = [[NEHangupParam alloc] init];
        [[NECallEngine sharedInstance] hangup:param
                                   completion:^(NSError *_Nullable error) {
                                     if (error) {
                                       NSLog(@"audio hangup - error -- %@", error.description);
                                     } else {
                                       BOOL isAudio = (self.enterStatus == audio_call ||
                                                       self.enterStatus == audio_invited);
                                       if (isAudio) {
                                         [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
                                       } else {
                                         [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
                                       }
                                     }
                                   }];
        // 此处主动调用还是需要,(正常情况下oncallEnd回调会回来)因为断网情况下，需要快速响应
        [self endRoom];
      }

      break;

      default:
        break;
    }
  };

  self.connectingView.itemExpand = ^(Item item, BOOL close) {
    __strong typeof(weakSelf) self = weakSelf;
    switch (item) {
      case item_mic: {
        if ([self isVirtualRoom]) {
          return;
        }
        [[NECallEngine sharedInstance] muteLocalAudio:close];
      }

      break;
      case item_speaker: {
        if ([self isVirtualRoom]) {
          [NEOneOnOnePlayerUtil.getInstance changePlayerModelSpeaker:!close];
          return;
        }
        if ([NEOneOnOnePlayerUtil.getInstance isHeadSetPlugging]) {
          return;
        }
        int ret = [NERtcEngine.sharedEngine setLoudspeakerMode:!close];
        if (ret != 0) {
          [NEOneOnOneLog infoLog:tag
                            desc:[NSString stringWithFormat:@"%d:setLoudSpeakerMode", ret]];
        }
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
  if ([self isVirtualRoom]) {
    self.videoConnectedView.canChangeView = NO;
  }

  [self.view addSubview:self.giftButton];
  [self.giftButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.view).offset(-15);
    make.width.height.equalTo(@40);
    make.bottom.equalTo(self.view).offset(-170);
  }];
  self.videoConnectedView.hidden = YES;

  self.videoConnectedView.itemExpand = ^(Item item, BOOL close) {
    __strong typeof(weakSelf) self = weakSelf;
    switch (item) {
      case item_close: {
        if (self.timer) {
          [self.timer invalidate];
        }

        if ([self isVirtualRoom]) {
          BOOL isAudio = (self.enterStatus == audio_call);
          if (isAudio) {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
          } else {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
          }
          [self endRoom];
          return;
        }
        self.hasEndRoom = @"end";
        NEHangupParam *param = [[NEHangupParam alloc] init];
        [[NECallEngine sharedInstance] hangup:param
                                   completion:^(NSError *_Nullable error) {
                                     if (error) {
                                       NSLog(@"video hangup - error -- %@", error.description);
                                     } else {
                                       [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
                                     }
                                   }];
        // 此处主动调用还是需要,(正常情况下oncallEnd回调会回来)因为断网情况下，需要快速响应
        [self endRoom];
      } break;

      case item_mic: {
        if ([self isVirtualRoom]) {
          return;
        }
        [[NECallEngine sharedInstance] muteLocalAudio:close];
      }

      break;
      case item_speaker: {
        if ([self isVirtualRoom]) {
          [NEOneOnOnePlayerUtil.getInstance changePlayerModelSpeaker:!close];
          return;
        }
        if ([NEOneOnOnePlayerUtil.getInstance isHeadSetPlugging]) {
          return;
        }

        int ret = [NERtcEngine.sharedEngine setLoudspeakerMode:!close];
        if (ret != 0) {
          [NEOneOnOneLog infoLog:tag
                            desc:[NSString stringWithFormat:@"%d:setLoudSpeakerMode", ret]];
        }
      } break;
      case item_switch_camera: {
        if ([self isVirtualRoom]) {
          [self switchCamera];
          return;
        }
        [[NECallEngine sharedInstance] switchCamera];
      }

      break;

      case item_video_close: {
        if ([self isVirtualRoom]) {
          return;
        }
        [[NECallEngine sharedInstance] muteLocalVideo:close];
      }

      break;
      case item_video_change: {
        /// 如果是虚拟房间，不做大小屏切换
        if ([self isVirtualRoom]) {
          return;
        }
        // 数据记录
        self.videoConverted = close;
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

  if ([self isVirtualRoom]) {
    dispatch_time_t delayTime =
        dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3.0 * NSEC_PER_SEC));  // 延迟 3 秒执行
    dispatch_after(delayTime, dispatch_get_main_queue(), ^{
      __strong typeof(weakSelf) self = weakSelf;
      // 在指定时间后执行的任务
      [self userAccept];
      [self playMedia];
    });
  } else {
    // 添加小窗功能
    [self.view addSubview:self.smallWindowButton];
    [self.smallWindowButton mas_makeConstraints:^(MASConstraintMaker *make) {
      make.right.equalTo(self.view).offset(-15);
      make.width.height.equalTo(@40);
      make.bottom.equalTo(self.giftButton.mas_top).offset(-16);
    }];
    [self setupSmallWindown];
  }
}

- (void)enterViewController {
  // 根据参数处理内容
  switch (self.enterStatus) {
    case audio_call: {
      [self.connectingView updateUI:self.callParam.remoteAvatar
                         remoteName:self.callParam.remoteShowName
                             status:audio_call_start];
      if ([self isVirtualRoom]) {
        return;
      }

      /// 呼叫中的音乐也需要为听筒模式，所以在此处设置，不在接通后设置
      int ret = [NERtcEngine.sharedEngine setLoudspeakerMode:NO];
      if (ret != 0) {
        [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%d:setLoudSpeakerMode", ret]];
      }
    } break;
    case video_call: {
      [self.connectingView updateUI:self.callParam.remoteAvatar
                         remoteName:self.callParam.remoteShowName
                             status:video_call_start];
      [[NERtcEngine sharedEngine] setParameters:@{kNERtcKeyVideoStartWithBackCamera : @NO}];
    } break;
    case audio_invited: {
      [self.connectingView updateUI:self.callParam.remoteAvatar
                         remoteName:self.callParam.remoteShowName
                             status:audio_invide_start];
      /// 呼叫中的音乐也需要为听筒模式，所以在此处设置，不在接通后设置
      int ret = [NERtcEngine.sharedEngine setLoudspeakerMode:NO];
      if (ret != 0) {
        [NEOneOnOneLog infoLog:tag desc:[NSString stringWithFormat:@"%d:setLoudSpeakerMode", ret]];
      }
    } break;

    case video_invited: {
      [[NERtcEngine sharedEngine] setParameters:@{kNERtcKeyVideoStartWithBackCamera : @NO}];
      [self.connectingView updateUI:self.callParam.remoteAvatar
                         remoteName:self.callParam.remoteShowName
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
    _videoConnectedView = [[NEOneOnOneVideoConnectedView alloc] initWithFrame:self.view.bounds];
  }
  return _videoConnectedView;
}

- (UIButton *)giftButton {
  if (!_giftButton) {
    _giftButton = [[UIButton alloc] init];
    [_giftButton setImage:[NEOneOnOneUI ne_imageName:@"gift_ico"] forState:UIControlStateNormal];
    [_giftButton addTarget:self
                    action:@selector(chosenGift)
          forControlEvents:UIControlEventTouchUpInside];
    _giftButton.hidden = YES;
    _giftButton.accessibilityIdentifier = @"id.sendGift";
  }
  return _giftButton;
}
- (UIButton *)smallWindowButton {
  if (!_smallWindowButton) {
    _smallWindowButton = [[UIButton alloc] init];
    [_smallWindowButton setImage:[NEOneOnOneUI ne_imageName:@"small_window"]
                        forState:UIControlStateNormal];
    [_smallWindowButton addTarget:self
                           action:@selector(smallWindowEvent)
                 forControlEvents:UIControlEventTouchUpInside];
    _smallWindowButton.hidden = YES;
    _smallWindowButton.accessibilityIdentifier = @"id.smallWindow";
  }
  return _smallWindowButton;
}

- (void)chosenGift {
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"oneOnOneGift" object:nil]];
  [NEOneOnOneGiftViewController showWithViewController:self delegate:self];
}

- (void)smallWindowEvent {
  // 开启小窗
  [self changeToSmall];
  self.coverView.hidden = !self.remoteVideoMute;
}

// 恢复的时候需要刷新
- (void)changeToNormal {
  self.coverView.hidden = YES;
  [super changeToNormal];
  [self refreshVideoView:self.videoConverted];
}
- (void)giftView:(NEOneOnOneGiftViewController *)giftView
        sendGift:(NEOneOnOneGiftItem *)gift
           count:(NSInteger)count {
  if (self.reachability.currentReachabilityStatus == NotReachable) {
    [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
    return;
  }
  __weak typeof(self) weakSelf = self;
  [[NEOneOnOneKit getInstance]
      rewardWithGiftId:gift.giftId
             giftCount:count
                target:self.callParam.remoteUserAccid
              callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                if (code == 0) {
                  [weakSelf
                      playGiftWithName:[NSString stringWithFormat:@"anim_gift_0%zd", gift.giftId]];
                }
              }];
}

- (void)onReceiveGiftWithGift:(NEOneOnOneOneGift *)gift {
  if (![gift.senderUserUuid isEqualToString:self.callParam.remoteUserAccid]) {
    return;
  }
  [self playGiftWithName:[NSString stringWithFormat:@"anim_gift_0%zd", gift.giftId]];
  NSString *giftName = @"个荧光棒";
  switch (gift.giftId) {
    case 1:
      giftName = @"个荧光棒";
      break;
    case 2:
      giftName = @"个安排";
      break;
    case 3:
      giftName = @"个跑车";
      break;
    case 4:
      giftName = @"个火箭";
      break;
    default:
      break;
  }
  if (!self.isSmallWindow) {
    [NEOneOnOneToast
        showToast:[NSString stringWithFormat:@"%@ %zd %@", NELocalizedString(@"你收到"),
                                             gift.giftCount, NELocalizedString(giftName)]];
  }
}

#pragma mark - gift animation

/// 播放礼物动画
- (void)playGiftWithName:(NSString *)name {
  dispatch_async(dispatch_get_main_queue(), ^{
    // 必须放在主线程
    if (UIApplication.sharedApplication.applicationState == UIApplicationStateBackground) {
      // 在后台就不添加礼物动画了
      return;
    }
    [self.view addSubview:self.giftAnimation];
    [self.view bringSubviewToFront:self.giftAnimation];
    [self.giftAnimation addGift:name];
  });
}

- (NEOneOnOneGiftView *)giftAnimation {
  if (!_giftAnimation) {
    _giftAnimation = [[NEOneOnOneGiftView alloc] init];
  }
  return _giftAnimation;
}

- (void)userAccept {
  __weak typeof(self) weakSelf = self;
  NSLog(@"用户同意");
  if (self.enterStatus == audio_call || self.enterStatus == audio_invited) {
    dispatch_async(dispatch_get_main_queue(), ^{
      __strong typeof(weakSelf) self = weakSelf;
      self.connectingView.hidden = NO;
      self.videoConnectedView.hidden = YES;
      self.giftButton.hidden = NO;
      self.smallWindowButton.hidden = NO;
      [self.connectingView updateUI:self.callParam.remoteAvatar
                         remoteName:self.callParam.remoteShowName
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
              __strong typeof(weakSelf) self = weakSelf;
              [self timeUp];
            }
               interval:1
                 repeat:YES
                  async:YES
        reuseIdentifier:@"NEOneOnOneCallViewController"];

  } else if (self.enterStatus == video_call || self.enterStatus == video_invited) {
    [self refreshVideoUI];
    if ([self isVirtualRoom]) {
      [self localPreview];
    }
  }
}

// 自行调用摄像头，如果后期需要快速切换，可以使用这个
- (void)localPreview {
  self.captureSession = [[AVCaptureSession alloc] init];
  self.captureSession.sessionPreset = AVCaptureSessionPresetPhoto;

  // 获取可用的摄像头设备
  NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
  AVCaptureDevice *newDevice = nil;
  for (AVCaptureDevice *device in devices) {
    if (device.position == AVCaptureDevicePositionFront) {
      newDevice = device;
      break;
    }
  }

  NSError *error = nil;
  AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:newDevice error:&error];
  if (input) {
    [self.captureSession addInput:input];
    self.input = input;

    AVCaptureVideoPreviewLayer *previewLayer =
        [AVCaptureVideoPreviewLayer layerWithSession:self.captureSession];
    previewLayer.frame = self.videoConnectedView.localVideoView.bounds;
    previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    [self.videoConnectedView.localVideoView.layer addSublayer:previewLayer];

    AVCaptureVideoDataOutput *dataOutput = [[AVCaptureVideoDataOutput alloc] init];
    [dataOutput setVideoSettings:@{
      (NSString *)kCVPixelBufferPixelFormatTypeKey : @(kCVPixelFormatType_32BGRA)
    }];
    [dataOutput setSampleBufferDelegate:self queue:dispatch_queue_create("videoQueue", NULL)];
    [self.captureSession addOutput:dataOutput];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      [self.captureSession startRunning];
    });
  } else {
    NSLog(@"Error: %@", error);
  }
}

- (void)switchCamera {
  // 停止当前正在运行的 AVCaptureSession
  [self.captureSession stopRunning];

  // 获取可用的摄像头设备
  NSArray *devices = [AVCaptureDevice devicesWithMediaType:AVMediaTypeVideo];
  AVCaptureDevice *currentDevice = [self.input device];
  AVCaptureDevicePosition position = [currentDevice position];
  AVCaptureDevice *newDevice = nil;
  for (AVCaptureDevice *device in devices) {
    if (position == AVCaptureDevicePositionBack &&
        device.position == AVCaptureDevicePositionFront) {
      newDevice = device;
      break;
    } else if (position == AVCaptureDevicePositionFront &&
               device.position == AVCaptureDevicePositionBack) {
      newDevice = device;
      break;
    }
  }

  // 创建一个新的 AVCaptureDeviceInput 对象
  NSError *error;
  AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:newDevice error:&error];
  if (!input) {
    NSLog(@"Error %@", error);
    return;
  }

  // 将新的 AVCaptureDeviceInput 添加到 AVCaptureSession 中
  [self.captureSession beginConfiguration];
  [self.captureSession removeInput:self.input];
  if ([self.captureSession canAddInput:input]) {
    [self.captureSession addInput:input];
    self.input = input;
  } else {
    NSLog(@"Can't add input");
    [self.captureSession addInput:input];
  }
  [self.captureSession commitConfiguration];

  // 启动新的 AVCaptureSession
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    [self.captureSession startRunning];
  });
}
- (void)userEnterVideo {
  if (self.enterStatus == video_call || self.enterStatus == video_invited) {
    [self refreshVideoView:NO];
  }
}

- (void)refreshVideoUI {
  __weak typeof(self) weakSelf = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    __strong typeof(weakSelf) self = weakSelf;
    self.connectingView.hidden = YES;
    self.videoConnectedView.hidden = NO;
    self.giftButton.hidden = NO;
    self.smallWindowButton.hidden = NO;
    [self.videoConnectedView updateUI:self.callParam.remoteAvatar
                           remoteName:self.callParam.remoteShowName];
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
            __strong typeof(weakSelf) self = weakSelf;
            [self timeUp];
          }
             interval:1
               repeat:YES
                async:YES
      reuseIdentifier:@"NEOneOnOneCallViewController"];
}

- (void)refreshVideoView:(BOOL)convert {
  dispatch_async(dispatch_get_main_queue(), ^{
    if ([self isVirtualRoom]) {
      [[NECallEngine sharedInstance] setupLocalView:convert
                                                        ? self.videoConnectedView.remoteVideoView
                                                        : self.videoConnectedView.localVideoView];
    } else {
      [[NECallEngine sharedInstance] setupLocalView:convert
                                                        ? self.videoConnectedView.remoteVideoView
                                                        : self.videoConnectedView.localVideoView];
      NSLog(@"self.status == NERtcCallStatusInCall enableLocalVideo:YES");
      [[NECallEngine sharedInstance] setupRemoteView:convert
                                                         ? self.videoConnectedView.localVideoView
                                                         : self.videoConnectedView.remoteVideoView];
    }
  });
}

/// 摄像头变化回调
- (void)onVideoMute:(BOOL)mute userID:(NSString *)userID {
  if ([self isLocalUser:(long)userID]) {
    // 本端用户
    [self.videoConnectedView showLocalBlackView:mute];
  } else {
    [self.videoConnectedView showRemoteBlackView:mute];
    self.remoteVideoMute = mute;
  }
}

- (void)timeUp {
  self.timerCount += 1;
  if (self.enterStatus == audio_call || self.enterStatus == audio_invited) {
    NSString *timeString = [self getMMSSFromSS:self.timerCount];
    [self.connectingView updateTimer:timeString];
    dispatch_async(dispatch_get_main_queue(), ^{
      self.audioSmallViewTimerLabel.text = timeString;
    });
  } else if (self.enterStatus == video_call || self.enterStatus == video_invited) {
    NSString *timeString = [self getMMSSFromSS:self.timerCount];
    [self.videoConnectedView updateTimer:timeString];
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
  if ([self isVirtualRoom]) {
    dispatch_async(dispatch_get_main_queue(), ^{
      if (self.presentedViewController) {
        [self.presentedViewController dismissViewControllerAnimated:NO
                                                         completion:^{
                                                           [self dismissViewControllerAnimated:YES
                                                                                    completion:nil];
                                                         }];
      } else {
        [self dismissViewControllerAnimated:YES
                                 completion:^{
                                 }];
      }
    });
  } else {
    [[NSNotificationCenter defaultCenter] postNotificationName:kCallKitDismissNoti object:nil];
  }
}

- (void)playMedia {
  if (self.enterStatus == audio_call) {
    // 音频
    if (self.remoteUser.audioUrl.length > 0) {
      [self playAudio:self.remoteUser.audioUrl];
    } else {
      [self endRoom];
    }
  } else {
    // 视频
    if (self.enterStatus == video_call) {
      if (self.remoteUser.videoUrl.length > 0) {
        [self playVideo:self.remoteUser.videoUrl];
      } else {
        [self endRoom];
      }
    }
  }
}

- (void)playAudio:(NSString *)urlString {
  [NEOneOnOnePlayerUtil.getInstance playAudio:urlString];
}

- (void)playVideo:(NSString *)urlString {
  [[NEOneOnOnePlayerUtil getInstance] playVideo:urlString
                                          frame:self.videoConnectedView.remoteVideoView.bounds
                                     superLayer:self.videoConnectedView.remoteVideoView.layer];
}
- (void)dealloc {
  if ([self isVirtualRoom] && self.originalCategory) {
    [AVAudioSession.sharedInstance setCategory:self.originalCategory error:nil];
  }
  if (self.enterStatus == video_call && [self isVirtualRoom]) {
    if (self.captureSession) {
      [self.captureSession stopRunning];
      // 将会话中的输入和输出设置为 nil
      for (AVCaptureInput *input in self.captureSession.inputs) {
        [self.captureSession removeInput:input];
      }
      for (AVCaptureOutput *output in self.captureSession.outputs) {
        [self.captureSession removeOutput:output];
      }

      // 将会话对象设置为 nil 或者使用 weak 引用
      self.captureSession = nil;
    }
  }
  NSLog(@"dealloc - 控制器释放");
  [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
  /// 恢复其他音乐播放
  [[AVAudioSession sharedInstance] setActive:NO
                                 withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
                                       error:nil];

  [[NEOneOnOneKit getInstance] removeOneOnOneListener:self];
  [[NEOneOnOneKit getInstance] removeAuthListener:self];
  [NEOneOnOnePlayerUtil.getInstance removePlayerListener:self];
  [self destroyNetworkObserver];
  //  [[NSNotificationCenter defaultCenter] postNotificationName:@"kCallKitDismissNoti" object:nil];
  /// 状态机置为空闲
  [[NECallEngine sharedInstance] changeStatusIdle];
}

#pragma mark NEOneOnOnePlayerProtocol

- (void)OneOnOnePlayerDidFinishPlaying {
  [NEOneOnOnePlayerUtil.getInstance resetPlayer];
  [NEOneOnOneToast showToast:NELocalizedString(@"通话已结束哦~")];
  [self endRoom];
}

- (void)OneOnOnePlayerErrorPlaying {
  [NEOneOnOnePlayerUtil.getInstance resetPlayer];
  [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请重新拨打")];
  [self endRoom];
}
- (void)OneOnOnePlayerInterruption:(NSNotification *)notification {
  NSDictionary *userInfo = notification.userInfo;
  AVAudioSessionInterruptionType type =
      [userInfo[AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];

  switch (type) {
    case AVAudioSessionInterruptionTypeBegan: {
      // 暂停视频播放
      dispatch_async(dispatch_get_main_queue(), ^{
        [[NEOneOnOnePlayerUtil getInstance] playerPause];
      });

      break;
    }
    case AVAudioSessionInterruptionTypeEnded: {
      AVAudioSessionInterruptionOptions options =
          [userInfo[AVAudioSessionInterruptionOptionKey] unsignedIntegerValue];
      if (options & AVAudioSessionInterruptionOptionShouldResume) {
        // 恢复视频播放
        dispatch_async(dispatch_get_main_queue(), ^{
          [[NEOneOnOnePlayerUtil getInstance] playerPlay];
        });
      }
      break;
    }
    default:
      break;
  }
}

- (BOOL)isLocalUser:(long)uid {
  if ([NEOneOnOneKit getInstance].localMember.rtcUid == uid) {
    return YES;
  }
  return NO;
}

#pragma mark OneOnOneListener

- (void)onReceiveNotificationCustomMessageWithMessage:(NEOneOnOneCustomMessage *)message {
  if (message.type == 400 || message.type == 401) {
    if (message.data.audio) {
      long uid = message.data.audio.uid;
      if ([self isLocalUser:uid]) {
        [NEOneOnOneToast showToast:NELocalizedString(@"您的言语涉及敏感内容，请文明用语哦~")];
      }
    }
    if (message.data.video) {
      long uid = message.data.video.uid;
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
  __weak typeof(self) weakSelf = self;
  dispatch_queue_t queue = async ? self.timeQueue : dispatch_get_main_queue();
  // 穿件定时器
  dispatch_source_t timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, queue);
  // 开始时间
  dispatch_time_t start = dispatch_time(DISPATCH_TIME_NOW, 0 * NSEC_PER_SEC);
  // 设置各种时间
  dispatch_source_set_timer(timer, start, interval * NSEC_PER_SEC, 0);
  // 设置回调
  dispatch_source_set_event_handler(timer, ^{
    __strong typeof(weakSelf) self = weakSelf;
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
- (void)onOneOnOneAuthEvent:(enum NEOneOnOneAuthEvent)event {
  if (event == NEOneOnOneAuthEventKickOut) {
    if ([self isVirtualRoom]) {
      [[NEOneOnOnePlayerUtil getInstance] resetPlayer];
      [self endRoom];
    }
  }
}
- (BOOL)isVirtualRoom {
  return (self.remoteUser.oc_callType == 1);
}

// 自定义数据解析
- (void)extraParamsDataAnalysis {
  if (self.remoteUser.oc_callType == 1) {
    // 虚拟房间。手动加入和退出
    return;
  }

  if (self.callParam.isCaller) {
    // 呼叫方
    if (self.callParam.callType == NERtcCallTypeAudio) {
      self.enterStatus = audio_call;
    } else {
      self.enterStatus = video_call;
    }
  } else {
    // 被呼叫方
    if (self.callParam.callType == NERtcCallTypeAudio) {
      self.enterStatus = audio_invited;
    } else {
      self.enterStatus = video_invited;
    }
  }

  //    NSError *error = nil;
  //    NSData *jsonData = [self.callParam.extra dataUsingEncoding:NSUTF8StringEncoding];
  //    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:jsonData
  //    options:NSJSONReadingMutableContainers error:&error]; if (error == nil) {
  ////        self.remoteUser = [[NEOneOnOneOnlineUser alloc] init];
  ////        //音频播放地址
  ////        self.remoteUser.audioUrl = dictionary[@"audioPlayUrl"];
  ////        //视频播放地址
  ////        self.remoteUser.videoUrl = dictionary[@"videoPlayUrl"];
  ////        //是否是虚拟房间
  ////        self.remoteUser.oc_callType = [dictionary[@"callType"] intValue];
  //        ///呼叫类型
  //
  //    } else {
  //        // 解析错误处理
  //        [NEOneOnOneLog
  //            infoLog:tag
  //               desc:[NSString stringWithFormat:@"%@:extraParams:%@", tag,self.callParam.extra]];
  //        [self endRoom];
  //    }
}
@end
