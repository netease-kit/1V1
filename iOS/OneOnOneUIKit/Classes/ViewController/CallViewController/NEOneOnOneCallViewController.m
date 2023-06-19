// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneCallViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <Masonry/Masonry.h>
#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NEOneOnOneKit/NEOneOnOneLog.h>
#import <NEOneOnOneUIKit/NEOneOnOneUIKit-Swift.h>
#import <NERtcCallKit/NERtcCallKit.h>
#import <NEUIKit/NEUIKit.h>
#import <libextobjc/extobjc.h>
#import "NEOneOnOneCallViewController+RtcCall.h"
#import "NEOneOnOneCustomTimer.h"
#import "NEOneOnOneGiftView.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneReachability.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUI.h"
#import "NEOneOnOneUIKitUtils.h"
#import "NERtcCallKit+Party.h"
@interface NEOneOnOneCallViewController () <NEOneOnOneGiftViewDelegate,
                                            AVCaptureVideoDataOutputSampleBufferDelegate,
                                            NEOneOnOneAuthListener>

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
@property(nonatomic, strong) AVPlayerLayer *playerLayer;
@property(nonatomic, strong) AVPlayer *player;
@property(nonatomic, strong) AVCaptureDeviceInput *input;
@property(nonatomic, strong) AVAudioSessionCategory originalCategory;

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
  if (self.player) {
    [self.player play];
  }
}

- (void)viewDidLoad {
  [super viewDidLoad];
  self.view.backgroundColor = [UIColor blackColor];
  [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
  NSError *active_err;
  NSError *setCategory_err;
  //    try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayAndRecord, with:
  //    .duckOthers)
  if (self.remoteUser.oc_callType == 1) {
    // 虚拟房间
    // 处理后台播放音乐不会自动暂停以及设置category导致在后台对端接听后无声音的问题；虚拟房间进行设置，销毁进行恢复
    self.originalCategory = AVAudioSession.sharedInstance.category;
    [AVAudioSession.sharedInstance setCategory:AVAudioSessionCategoryPlayback
                                         error:&setCategory_err];
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
  //    self.navigationController.navigationBar.hidden = YES;
  self.ne_UINavigationItem.navigationBarHidden = YES;
  [self.view addSubview:self.connectingView];
  [[NEOneOnOneKit getInstance] addOneOnOneListener:self];
  [[NEOneOnOneKit getInstance] addAuthListener:self];
  [NERtcCallKit sharedInstance].engineDelegate = self;
  @weakify(self) self.connectingView.itemEvent = ^(Item item) {
    @strongify(self) switch (item) {
      case item_cancel: {
        if (self.remoteUser.oc_callType == 1) {
          [self endRoom];
          return;
        }
        @weakify(self)[[NERtcCallKit sharedInstance] cancel:^(NSError *_Nullable error) {
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
        if (self.remoteUser.oc_callType == 1) {
          BOOL isAudio = (self.enterStatus == audio_call);
          if (isAudio) {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
          } else {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
          }
          [self endRoom];
          return;
        }

        [[NERtcCallKit sharedInstance] hangup:^(NSError *_Nullable error) {
          if (error) {
            NSLog(@"audio hangup - error -- %@", error.description);
          } else {
            BOOL isAudio = (self.enterStatus == audio_call || self.enterStatus == audio_invited);
            if (isAudio) {
              [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
            } else {
              [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
            }
          }
        }];
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
        if (self.remoteUser.oc_callType == 1) {
          return;
        }
        [[NERtcCallKit sharedInstance] muteLocalAudio:close];
      }

      break;
      case item_speaker: {
        if (self.player) {
          self.player.volume = close ? 0 : [[AVAudioSession sharedInstance] outputVolume];
          return;
        }
        [NERtcEngine.sharedEngine adjustUserPlaybackSignalVolume:close ? 0 : 100
                                                       forUserID:self.remoteUserId];
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
  if (self.remoteUser.oc_callType == 1) {
    self.videoConnectedView.canChangeView = NO;
  }
  [self.videoConnectedView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.top.right.bottom.equalTo(self.view);
  }];

  [self.view addSubview:self.giftButton];
  [self.giftButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.view).offset(-15);
    make.width.height.equalTo(@40);
    make.bottom.equalTo(self.view).offset(-170);
  }];
  self.videoConnectedView.hidden = YES;

  self.videoConnectedView.itemExpand = ^(Item item, BOOL close) {
    @strongify(self) switch (item) {
      case item_close: {
        if (self.timer) {
          [self.timer invalidate];
        }

        if (self.remoteUser.oc_callType == 1) {
          BOOL isAudio = (self.enterStatus == audio_call);
          if (isAudio) {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束通话")];
          } else {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
          }
          [self endRoom];
          return;
        }

        [[NERtcCallKit sharedInstance] hangup:^(NSError *_Nullable error) {
          if (error) {
            NSLog(@"video hangup - error -- %@", error.description);
          } else {
            [NEOneOnOneToast showToast:NELocalizedString(@"结束视频")];
            [self endRoom];
          }
        }];
        [self endRoom];
      } break;

      case item_mic: {
        if (self.remoteUser.oc_callType == 1) {
          return;
        }
        [[NERtcCallKit sharedInstance] muteLocalAudio:close];
      }

      break;
      case item_speaker: {
        [NERtcEngine.sharedEngine adjustUserPlaybackSignalVolume:close ? 0 : 100
                                                       forUserID:self.remoteUserId];
        if (self.player) {
          self.player.volume = close ? 0 : [[AVAudioSession sharedInstance] outputVolume];
        }
      } break;
      case item_switch_camera: {
        if (self.remoteUser.oc_callType == 1) {
          [self switchCamera];
          return;
        }
        [[NERtcCallKit sharedInstance] switchCamera];
      }

      break;

      case item_video_close: {
        //                [[NERtcCallKit sharedInstance] enableLocalVideo:!close];
        [[NERtcCallKit sharedInstance] muteLocalVideo:close];
      }

      break;
      case item_video_change: {
        /// 如果是虚拟房间，不做大小屏切换
        if (self.remoteUser.oc_callType == 1) {
          return;
        }
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

  if (self.remoteUser.oc_callType == 1) {
    dispatch_time_t delayTime =
        dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3.0 * NSEC_PER_SEC));  // 延迟 3 秒执行
    @weakify(self) dispatch_after(delayTime, dispatch_get_main_queue(), ^{
      @strongify(self)
          // 在指定时间后执行的任务
          [self userAccept];
      [self playMedia];
    });
  }
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

- (UIButton *)giftButton {
  if (!_giftButton) {
    _giftButton = [[UIButton alloc] init];
    [_giftButton setImage:[NEOneOnOneUI ne_imageName:@"gift_ico"] forState:UIControlStateNormal];
    [_giftButton addTarget:self
                    action:@selector(chosenGift)
          forControlEvents:UIControlEventTouchUpInside];
    _giftButton.hidden = YES;
  }
  return _giftButton;
}

- (void)chosenGift {
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"oneOnOneGift" object:nil]];
  [NEOneOnOneGiftViewController showWithViewController:self delegate:self];
}

- (void)giftView:(NEOneOnOneGiftViewController *)giftView
        sendGift:(NEOneOnOneGiftItem *)gift
           count:(NSInteger)count {
  if (self.reachability.currentReachabilityStatus == NotReachable) {
    [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
    return;
  }
  @weakify(self)[[NEOneOnOneKit getInstance]
      rewardWithGiftId:gift.giftId
             giftCount:count
                target:self.remoteUser.userUuid
              callback:^(NSInteger code, NSString *_Nullable msg, id _Nullable obj) {
                if (code == 0) {
                  @strongify(self)[self
                      playGiftWithName:[NSString stringWithFormat:@"anim_gift_0%zd", gift.giftId]];
                }
              }];
}

- (void)onReceiveGiftWithGift:(NEOneOnOneOneGift *)gift {
  if (![gift.senderUserUuid isEqualToString:self.remoteUser.userUuid]) {
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
  [NEOneOnOneToast
      showToast:[NSString stringWithFormat:@"%@ %zd %@", NELocalizedString(@"你收到"),
                                           gift.giftCount, NELocalizedString(giftName)]];
}

#pragma mark - gift animation

/// 播放礼物动画
- (void)playGiftWithName:(NSString *)name {
  if (UIApplication.sharedApplication.applicationState == UIApplicationStateBackground) {
    // 在后台就不添加礼物动画了
    return;
  }
  dispatch_async(dispatch_get_main_queue(), ^{
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
  @weakify(self) NSLog(@"用户同意");
  if (self.enterStatus == audio_call || self.enterStatus == audio_invited) {
    dispatch_async(dispatch_get_main_queue(), ^{
      @strongify(self) self.connectingView.hidden = NO;
      self.videoConnectedView.hidden = YES;
      self.giftButton.hidden = NO;
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
    if (self.remoteUser.oc_callType == 1) {
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
  @weakify(self) dispatch_async(dispatch_get_main_queue(), ^{
    @strongify(self) self.connectingView.hidden = YES;
    self.videoConnectedView.hidden = NO;
    self.giftButton.hidden = NO;
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
    if (self.remoteUser.oc_callType == 1) {
      [[NERtcCallKit sharedInstance] setupLocalView:convert
                                                        ? self.videoConnectedView.remoteVideoView
                                                        : self.videoConnectedView.localVideoView];
    } else {
      [[NERtcCallKit sharedInstance] setupLocalView:convert
                                                        ? self.videoConnectedView.remoteVideoView
                                                        : self.videoConnectedView.localVideoView];
      NSLog(@"self.status == NERtcCallStatusInCall enableLocalVideo:YES");
      [[NERtcCallKit sharedInstance]
          setupRemoteView:convert ? self.videoConnectedView.localVideoView
                                  : self.videoConnectedView.remoteVideoView
                  forUser:self.remoteUser.userUuid];
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
  NSURL *videoURL = [NSURL URLWithString:urlString];
  self.player = [AVPlayer playerWithURL:videoURL];
  self.playerLayer = [AVPlayerLayer playerLayerWithPlayer:self.player];
  [self.player play];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerDidFinishPlaying)
                                               name:AVPlayerItemDidPlayToEndTimeNotification
                                             object:self.player.currentItem];
}

- (void)playVideo:(NSString *)urlString {
  NSURL *videoURL = [NSURL URLWithString:urlString];
  self.player = [AVPlayer playerWithURL:videoURL];
  self.playerLayer = [AVPlayerLayer playerLayerWithPlayer:self.player];
  self.playerLayer.frame = self.videoConnectedView.remoteVideoView.bounds;
  self.playerLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
  [self.videoConnectedView.remoteVideoView.layer addSublayer:self.playerLayer];
  [self.player play];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerDidFinishPlaying)
                                               name:AVPlayerItemDidPlayToEndTimeNotification
                                             object:self.player.currentItem];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerDidFinishPlaying)
                                               name:AVPlayerItemDidPlayToEndTimeNotification
                                             object:self.player.currentItem];

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerErrorPlaying)
                                               name:AVPlayerItemFailedToPlayToEndTimeNotification
                                             object:self.player.currentItem];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerErrorPlaying)
                                               name:AVPlayerItemPlaybackStalledNotification
                                             object:self.player.currentItem];

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerErrorPlaying)
                                               name:AVPlayerItemNewErrorLogEntryNotification
                                             object:self.player.currentItem];

  // 监听 AVAudioSessionInterruptionNotification 通知
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(playerInterruption:)
                                               name:AVAudioSessionInterruptionNotification
                                             object:nil];
}
- (void)dealloc {
  if (self.remoteUser.oc_callType == 1 && self.originalCategory) {
    [AVAudioSession.sharedInstance setCategory:self.originalCategory error:nil];
  }
  if (self.enterStatus == video_call && self.remoteUser.oc_callType == 1) {
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
  [self destroyNetworkObserver];
  [[NSNotificationCenter defaultCenter] postNotificationName:@"kCallKitDismissNoti" object:nil];
  /// 状态机置为空闲
  [[NERtcCallKit sharedInstance] changeStatusIdle];
}
- (void)playerDidFinishPlaying {
  self.player = nil;
  [NEOneOnOneToast showToast:NELocalizedString(@"通话已结束哦~")];
  [self endRoom];
}

- (void)playerErrorPlaying {
  self.player = nil;
  [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请重新拨打")];
  [self endRoom];
}

- (BOOL)isLocalUser:(long)uid {
  if ([NEOneOnOneKit getInstance].localMember.rtcUid == uid) {
    return YES;
  }
  return NO;
}

- (void)playerInterruption:(NSNotification *)notification {
  NSDictionary *userInfo = notification.userInfo;
  AVAudioSessionInterruptionType type =
      [userInfo[AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];

  switch (type) {
    case AVAudioSessionInterruptionTypeBegan: {
      // 暂停视频播放
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.player pause];
      });

      break;
    }
    case AVAudioSessionInterruptionTypeEnded: {
      AVAudioSessionInterruptionOptions options =
          [userInfo[AVAudioSessionInterruptionOptionKey] unsignedIntegerValue];
      if (options & AVAudioSessionInterruptionOptionShouldResume) {
        // 恢复视频播放
        dispatch_async(dispatch_get_main_queue(), ^{
          [self.player play];
        });
      }
      break;
    }
    default:
      break;
  }
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
- (void)onOneOnOneAuthEvent:(enum NEOneOnOneAuthEvent)event {
  if (event == NEOneOnOneAuthEventKickOut) {
    if (self.remoteUser.oc_callType == 1 && self.player) {
      self.player = nil;
      [self endRoom];
    }
  }
}
@end
