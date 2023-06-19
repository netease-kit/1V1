// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneRoomListViewController.h"
#import <AVFoundation/AVFoundation.h>
#import <MJRefresh/MJRefresh.h>
#import <Masonry/Masonry.h>
#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
#import <NERtcCallKit/NERtcCallKit.h>
#import <NEUIKit/NEUIKit.h>
#import <NIMSDK/NIMSDK.h>
#import <ReactiveObjC/ReactiveObjC.h>
#import "NEOneOnOneBottomPresentView.h"
#import "NEOneOnOneCallViewController.h"
#import "NEOneOnOneEmptyListView.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneReachability.h"
#import "NEOneOnOneRoomListViewModel.h"
#import "NEOneOnOneToast.h"
#import "NEOneOnOneUI.h"
#import "NEOneOnOneUIDeviceSizeInfo.h"
#import "NEOneOnOneUIKitEngine.h"
#import "NEOneOnOneUIKitMacro.h"
#import "NEOneOnOneUIKitUtils.h"
#import "NEOneOnOneUILiveListCell.h"
#import "NEOneOnOneUserBusyView.h"
#import "NERtcCallKit+Party.h"

@interface NEOneOnOneRoomListViewController () <UICollectionViewDelegate,
                                                UICollectionViewDataSource,
                                                UIAlertViewDelegate>
@property(nonatomic, strong) UICollectionView *collectionView;
@property(nonatomic, strong) NEOneOnOneEmptyListView *emptyView;
@property(nonatomic, strong) NEOneOnOneRoomListViewModel *roomListViewModel;
/// 是否已进入房间，亦可做防重点击
@property(nonatomic, assign) BOOL isEnterRoom;
/// 在线人数
@property(nonatomic, assign) NSInteger onlineCount;
@property(nonatomic, strong) NEOneOnOneBottomPresentView *bottomPresentView;
@property(nonatomic, strong) NEOneOnOneUserBusyView *busyView;
/// 选中Info 信息
@property(nonatomic, strong) NEOneOnOneOnlineUser *roomInfoModel;
// 音频还是视频
@property(nonatomic, assign) BOOL isAudio;
/// 网络监听
@property(nonatomic, strong) NEOneOnOneReachability *reachability;

@end

@implementation NEOneOnOneRoomListViewController

- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
}

- (void)viewDidLoad {
  [super viewDidLoad];
  if (@available(iOS 13.0, *)) {
    UINavigationBarAppearance *appearance = [[UINavigationBarAppearance alloc] init];

    [appearance configureWithOpaqueBackground];

    NSMutableDictionary *textAttribute = [NSMutableDictionary dictionary];
    textAttribute[NSForegroundColorAttributeName] = [UIColor ne_colorWithHex:0x222222];  // 标题颜色
    textAttribute[NSFontAttributeName] = [UIFont systemFontOfSize:17];  // 标题大小
    [appearance setTitleTextAttributes:textAttribute];

    // 去除底部黑线
    [appearance setShadowImage:[UIImage ne_imageWithColor:[UIColor ne_colorWithHex:0xDCDDE0]]];

    UIColor *color = [UIColor whiteColor];
    appearance.backgroundColor = color;

    self.navigationController.navigationBar.standardAppearance = appearance;
    self.navigationController.navigationBar.scrollEdgeAppearance = appearance;
  }
  /// 如果要修改图片，需要修改LeftBar，并实现对应方法
  //    UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
  //    [btn setImage:[NEOneOnOneUI ne_imageName:@"online_icon"]
  //         forState:UIControlStateNormal];
  //    [btn setImage:[NEOneOnOneUI ne_imageName:@"online_icon"]
  //         forState:UIControlStateHighlighted];
  //    btn.frame = CGRectMake(0, 0, 30, 30);
  //    btn.contentEdgeInsets = UIEdgeInsetsMake(0, -15, 0, 0);
  //
  //    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:btn];

  // Do any additional setup after loading the view.
  self.title = NELocalizedString(@"1V1社交");
  self.view.backgroundColor = UIColor.whiteColor;

  [self getNewData];
  [self bindViewModel];
  [self setupSubviews];
  @weakify(self);
  [[NERtcCallKit sharedInstance]
      setPushConfigHandler:^(NERtcCallKitPushConfig *config, NERtcCallKitContext *context) {
        @strongify(self);
        if (self.isAudio) {
          config.pushContent =
              [NSString stringWithFormat:@"%@ %@", [NEOneOnOneKit getInstance].localMember.nickName,
                                         NELocalizedString(@"邀请您语音聊天")];
        } else {
          config.pushContent =
              [NSString stringWithFormat:@"%@ %@", [NEOneOnOneKit getInstance].localMember.nickName,
                                         NELocalizedString(@"邀请您视频聊天")];
        }
      }];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(busyViewShouHide:)
                                               name:NEOneOnOneCallViewControllerAppear
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(receiveInvite:)
                                               name:@"receiveInvite"
                                             object:nil];
}

- (void)receiveInvite:(NSNotification *)notification {
  [self.bottomPresentView dismiss:^{
  }];
}
- (void)busyViewShouHide:(NSNotification *)notification {
  [self.busyView removeFromSuperview];
}
- (void)getNewData {
  [self.roomListViewModel requestNewDataWithLiveType:NEOneOnOneLiveRoomTypeMultiAudio];
}
- (void)bindViewModel {
  @weakify(self);
  [RACObserve(self.roomListViewModel, datas) subscribeNext:^(NSArray *array) {
    @strongify(self);
    dispatch_async(dispatch_get_main_queue(), ^{
      dispatch_async(dispatch_get_main_queue(), ^{
        [self.collectionView reloadData];
        self.emptyView.hidden = [array count] > 0;
      });
    });
  }];

  [RACObserve(self.roomListViewModel, isLoading) subscribeNext:^(id _Nullable x) {
    @strongify(self);
    if (self.roomListViewModel.isLoading == NO) {
      [self.collectionView.mj_header endRefreshing];
      [self.collectionView.mj_footer endRefreshing];
      [NEOneOnOneToast hideLoading];
    } else {
      [NEOneOnOneToast showLoading];
    }
  }];

  [RACObserve(self.roomListViewModel, error) subscribeNext:^(NSError *_Nullable err) {
    if (!err || ![err isKindOfClass:[NSError class]]) return;
    if (err.code == 1003) {
      [NEOneOnOneToast showToast:NELocalizedString(@"直播列表为空")];
    } else {
      NSString *msg =
          err.userInfo[NSLocalizedDescriptionKey] ?: NELocalizedString(@"请求直播列表错误");
      [NEOneOnOneToast showToast:msg];
    }
  }];
}
- (void)setupSubviews {
  [[UIApplication sharedApplication].keyWindow addSubview:self.bottomPresentView];
  [self.view addSubview:self.collectionView];

  [self.collectionView addSubview:self.emptyView];

  [self.collectionView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.view).offset(16);
    make.right.equalTo(self.view).offset(-16);
    make.bottom.equalTo(self.view);
    make.height.mas_equalTo(UIScreenHeight - [NEOneOnOneUIDeviceSizeInfo get_iPhoneNavBarHeight]);
  }];

  [self.emptyView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.equalTo(self.collectionView);
    make.centerY.equalTo(self.collectionView).offset(-40);
  }];

  @weakify(self);
  MJRefreshGifHeader *mjHeader = [MJRefreshGifHeader headerWithRefreshingBlock:^{
    @strongify(self);
    [self.roomListViewModel requestNewDataWithLiveType:NEOneOnOneLiveRoomTypeMultiAudio];
  }];
  [mjHeader setTitle:NELocalizedString(@"下拉更新") forState:MJRefreshStateIdle];
  [mjHeader setTitle:NELocalizedString(@"下拉更新") forState:MJRefreshStatePulling];
  [mjHeader setTitle:NELocalizedString(@"更新中...") forState:MJRefreshStateRefreshing];
  mjHeader.lastUpdatedTimeLabel.hidden = YES;
  [mjHeader setTintColor:[UIColor whiteColor]];
  self.collectionView.mj_header = mjHeader;

  self.collectionView.mj_footer = [MJRefreshBackNormalFooter footerWithRefreshingBlock:^{
    @strongify(self);
    if (self.roomListViewModel.isEnd) {
      [NEOneOnOneToast showToast:NELocalizedString(@"无更多内容")];
      [self.collectionView.mj_footer endRefreshing];
    } else {
      [self.roomListViewModel requestMoreDataWithLiveType:NEOneOnOneLiveRoomTypeMultiAudio];
    }
  }];
}

- (void)dealloc {
  [NEOneOnOneToast hideLoading];
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark - UICollectionView delegate

- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
  return [self.roomListViewModel.datas count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                  cellForItemAtIndexPath:(NSIndexPath *)indexPath {
  return [NEOneOnOneUILiveListCell cellWithCollectionView:collectionView
                                                indexPath:indexPath
                                                    datas:self.roomListViewModel.datas];
}

- (void)collectionView:(UICollectionView *)collectionView
    didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
  if (self.isEnterRoom) {
    return;
  }
  if ([self.roomListViewModel.datas count] > indexPath.row) {
    NEOneOnOneOnlineUser *roomInfoModel = self.roomListViewModel.datas[indexPath.row];
    self.roomInfoModel = roomInfoModel;
    [self.bottomPresentView show:nil];
  }
}

- (void)audienceEnterLiveRoomWithListInfo:(NEOneOnOneOnlineUser *)info {
  NSLog(@"列表点击");
  //    NEVoiceRoomViewController *vc =
  //        [[NEVoiceRoomViewController alloc] initWithRole:NEVoiceRoomRoleAudience detail:info];
  //    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - lazy load
- (UICollectionView *)collectionView {
  if (!_collectionView) {
    UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
    layout.itemSize = [NEOneOnOneUILiveListCell size];
    layout.scrollDirection = UICollectionViewScrollDirectionVertical;
    layout.minimumInteritemSpacing = 8;
    layout.minimumLineSpacing = 8;

    _collectionView = [[UICollectionView alloc] initWithFrame:self.view.bounds
                                         collectionViewLayout:layout];
    _collectionView.backgroundColor = UIColor.whiteColor;
    [_collectionView registerClass:[NEOneOnOneUILiveListCell class]
        forCellWithReuseIdentifier:[NEOneOnOneUILiveListCell description]];
    _collectionView.delegate = self;
    _collectionView.dataSource = self;
    _collectionView.showsVerticalScrollIndicator = NO;
    if (@available(iOS 11.0, *)) {
      _collectionView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
    }
  }
  return _collectionView;
}

- (NEOneOnOneEmptyListView *)emptyView {
  if (!_emptyView) {
    _emptyView = [[NEOneOnOneEmptyListView alloc] initWithFrame:CGRectZero];
    _emptyView.tintColor = [UIColor ne_colorWithHex:0xE6E7EB];
  }
  return _emptyView;
}

- (NEOneOnOneRoomListViewModel *)roomListViewModel {
  if (!_roomListViewModel) {
    _roomListViewModel = [[NEOneOnOneRoomListViewModel alloc] init];
  }
  return _roomListViewModel;
}

- (NEOneOnOneBottomPresentView *)bottomPresentView {
  if (!_bottomPresentView) {
    @weakify(self) _bottomPresentView =
        [[NEOneOnOneBottomPresentView alloc] initWithFrame:[UIScreen mainScreen].bounds];
    _bottomPresentView.clickAudioAction = ^{
      @strongify(self) @weakify(self)
          // 选中音频通话1
          [self.bottomPresentView dismiss:^{
            @strongify(self) NSLog(@"点击音频通话");
            [self dealWithAction:YES];
          }];
    };
    _bottomPresentView.clickVideoAction = ^{
      @strongify(self) @weakify(self)
          // 选择视频通话
          [self.bottomPresentView dismiss:^{
            @strongify(self) NSLog(@"点击视频通话");
            [self dealWithAction:NO];
          }];
    };
    _bottomPresentView.clickChatUpAction = ^{
      @strongify(self) NetworkStatus status = [self.reachability currentReachabilityStatus];
      if (status == NotReachable) {
        [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
        return;
      }

      // 搭讪
      [self.bottomPresentView dismiss:^{
        NIMSession *session = [NIMSession session:self.roomInfoModel.userUuid
                                             type:NIMSessionTypeP2P];
        NIMMessage *message = [[NIMMessage alloc] init];
        NIMMessageSetting *setting = [[NIMMessageSetting alloc] init];
        setting.teamReceiptEnabled = YES;
        message.setting = setting;
        message.text = NELocalizedString(@"真心交友，愿意聊聊吗？\n很喜欢你呢~");
        NSError *error;
        [NIMSDK.sharedSDK.chatManager sendMessage:message toSession:session error:&error];
        if (error) {
        } else {
          [NEOneOnOneToast showToast:NELocalizedString(@"搭讪成功")];
        }
      }];
    };

    _bottomPresentView.clickPrivateLatterAction = ^{
      // 私信
      @strongify(self)[self.bottomPresentView dismiss:^{
        if (self.privateLatter) {
          self.privateLatter(self.roomInfoModel.userUuid);
        }
      }];
    };
  }
  return _bottomPresentView;
}
- (NEOneOnOneUserBusyView *)busyView {
  if (!_busyView) {
    _busyView = [[NEOneOnOneUserBusyView alloc] initWithFrame:[UIScreen mainScreen].bounds];
  }
  return _busyView;
}
// 具体处理
- (void)dealWithAction:(BOOL)isAudio {
  self.isAudio = isAudio;
  if (!self.roomInfoModel) {
    NSLog(@"无数据");
    return;
  }
  // 已点击通话数据
  self.isEnterRoom = YES;
  // 判断自身是否忙碌
  // 用户不在RTC房间中
  if ([NEOneOnOneUIKitEngine sharedInstance].canCall) {
    NSString *message = [NEOneOnOneUIKitEngine sharedInstance].canCall();
    if (message.length > 0) {
      /// 不能拨号
      /// 弹出提示框
      ///
#pragma mark TODO
      /// 需要确认是否要迁移到 all In One 中
      /// 1v1的业务逻辑
      /// all in one 才会存在
      [NEOneOnOneUIKitUtils presentAlertViewController:self
                                                titile:message
                                           cancelTitle:@""
                                          confirmTitle:NELocalizedString(@"确定")
                                       confirmComplete:nil];
      // 取消已点击通话数据
      self.isEnterRoom = NO;
    } else {
      [self startCallKitJudgeUserBusy:isAudio];
    }
  } else {
    // 外部未实现,则认为不需要处理，用户处于闲置中
    [self startCallKitJudgeUserBusy:isAudio];
  }
}

- (void)startCallKitJudgeUserBusy:(BOOL)isAudio {
  {
    @weakify(self)[[NEOneOnOneKit getInstance]
        getUserState:self.roomInfoModel.mobile
            callback:^(NSInteger code, NSString *_Nullable msg, NSString *_Nullable onlineState) {
              @strongify(self) if (code == 0) {
                // 请求成功
                if (onlineState.length > 0 && [onlineState isEqualToString:@"online"]) {
                  // 在线
                  /// 直接呼叫，对端收到邀请通知，如果在忙的话，hungUp配上原因，本段可以拿到状态
                  [self startCallAction:isAudio];

                } else {
                  // 不在线
                  [NEOneOnOneUIKitUtils
                      presentAlertViewController:self
                                          titile:NELocalizedString(@"对方不在线，请稍后再试")
                                     cancelTitle:@""
                                    confirmTitle:NELocalizedString(@"确定")
                                 confirmComplete:^{

                                 }];
                  // 取消已点击通话数据
                  self.isEnterRoom = NO;
                }
              }
              else {
                // 请求失败
                [NEOneOnOneToast showToast:NELocalizedString(@"网络异常，请稍后重试")];
                // 取消已点击通话数据
                self.isEnterRoom = NO;
              }
            }];
  }
}

- (void)startCallAction:(BOOL)isAudio {
  {
    __block BOOL hasPermissions = NO;
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);

    if (isAudio) {
      [NEOneOnOneUIKitUtils
          getMicrophonePermissions:AVMediaTypeAudio
                          complete:^(BOOL authorized) {
                            if (authorized) {
                              hasPermissions = YES;
                              dispatch_semaphore_signal(semaphore);
                            } else {
                              [NEOneOnOneToast
                                  showToast:NELocalizedString(@"麦克风权限已关闭，请开启后重试")];
                              dispatch_semaphore_signal(semaphore);
                            }
                          }];
    } else {
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
                                                                        @"摄像头权限已关闭，请开启"
                                                                        @"后重试")];
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
                                                                        @"麦克风权限已关闭，请开启"
                                                                        @"后重试")];
                                                      dispatch_semaphore_signal(semaphore);
                                                    } else {
                                                      /// 权限被拒
                                                      [NEOneOnOneToast
                                                          showToast:NELocalizedString(
                                                                        @"麦克风和摄像头权限已关闭"
                                                                        @"，请开启后重试")];
                                                      dispatch_semaphore_signal(semaphore);
                                                    }
                                                  }];
                            }
                          }];
    }
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    if (hasPermissions) {
    } else {
      self.isEnterRoom = NO;
      return;
    }
    NSLog(@"权限判断通过");
  }

  if (self.roomInfoModel.oc_callType != 1) {
    [NERtcCallKit sharedInstance].timeOutSeconds = isAudio ? 15 : 30;
  }

  NSDictionary *attachment = @{
    CALLER_USER_NAME : [NEOneOnOneKit getInstance].localMember.nickName,
    CALLER_USER_MOBILE : [NEOneOnOneKit getInstance].localMember.mobile,
    CALLER_USER_AVATAR : [NEOneOnOneKit getInstance].localMember.avatar
  };
  NSString *jsonString = [[NSString alloc]
      initWithData:[NSJSONSerialization dataWithJSONObject:attachment
                                                   options:NSJSONWritingPrettyPrinted
                                                     error:nil]
          encoding:NSUTF8StringEncoding];
  @weakify(self) dispatch_async(dispatch_get_main_queue(), ^{
    @strongify(self) NEOneOnOneCallViewController *callViewController =
        [[NEOneOnOneCallViewController alloc] init];
    callViewController.busyBlock = ^{
      dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication].keyWindow addSubview:self.busyView];
      });
    };
    if (self.roomInfoModel.oc_callType == 1) {
      [[NERtcCallKit sharedInstance] changeStatusCalling];
      [self startCallKit:isAudio controller:callViewController];
      return;
    }
    [[NERtcCallKit sharedInstance] addDelegate:callViewController];
    @weakify(
        self)[[NERtcCallKit sharedInstance] call:self.roomInfoModel.userUuid
                                            type:(isAudio) ? NERtcCallTypeAudio : NERtcCallTypeVideo
                                      attachment:jsonString
                                     globalExtra:nil
                                       withToken:nil
                                     channelName:nil
                                      completion:^(NSError *_Nullable error) {
                                        @strongify(self) NSLog(@"%@", error);
                                        if (error.code != 0) {
                                          // 未接通
                                          [NEOneOnOneToast
                                              showToast:NELocalizedString(@"呼叫未成功发出")];
                                          // 取消已点击通话数据
                                          self.isEnterRoom = NO;
                                        } else {
                                          [self startCallKit:isAudio controller:callViewController];
                                        }
                                      }];
  });
}
- (void)startCallKit:(BOOL)isAudio controller:(NEOneOnOneCallViewController *)callViewController {
  // 用户不在RTC房间中
  // 自身不在RTC中
  // 进入呼叫流程
  NEOneOnOneOnlineUser *remoteUser = [[NEOneOnOneOnlineUser alloc] init];

  remoteUser.userUuid = self.roomInfoModel.userUuid;
  remoteUser.userName = self.roomInfoModel.userName;
  remoteUser.mobile = self.roomInfoModel.mobile;
  remoteUser.icon = self.roomInfoModel.icon;
  remoteUser.oc_callType = self.roomInfoModel.oc_callType;
  remoteUser.audioUrl = self.roomInfoModel.audioUrl;
  remoteUser.videoUrl = self.roomInfoModel.videoUrl;
  callViewController.remoteUser = remoteUser;
  if (isAudio) {
    callViewController.enterStatus = audio_call;
  } else {
    callViewController.enterStatus = video_call;
  }
  callViewController.modalPresentationStyle = UIModalPresentationOverFullScreen;
  [self presentViewController:callViewController animated:YES completion:nil];
  //  [self.navigationController pushViewController:callViewController animated:YES];
  // 取消已点击通话数据
  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)),
                 dispatch_get_main_queue(), ^{
                   self.isEnterRoom = NO;
                 });
}
- (NEOneOnOneReachability *)reachability {
  if (!_reachability) {
    _reachability = [NEOneOnOneReachability reachabilityForInternetConnection];
  }
  return _reachability;
}

@end
