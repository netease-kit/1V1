// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneConnectingView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import <SDWebImage/SDWebImage.h>
#import "NEOneOnOneAudioButtomView.h"
#import "NEOneOnOneCustomButtonView.h"
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneUI.h"
@interface NEOneOnOneConnectingView ()
@property(nonatomic, strong) UIImageView *backGroundImageView;
@property(nonatomic, strong) UIImageView *remoteImageView;
@property(nonatomic, strong) UIView *whiteCircleView;
@property(nonatomic, strong) UILabel *remoteNameLabel;
@property(nonatomic, strong) UILabel *connectingStatusLabel;
@property(nonatomic, strong) UILabel *connectingStatusDescLabel;
/// 底部按钮 采用UIView 包含 Button + Label的形式
@property(nonatomic, strong) NEOneOnOneCustomButtonView *cancelView;
@property(nonatomic, strong) NEOneOnOneCustomButtonView *rejectView;
@property(nonatomic, strong) NEOneOnOneCustomButtonView *acceptView;
@property(nonatomic, strong) NEOneOnOneAudioButtomView *audioButtomView;

@end
@implementation NEOneOnOneConnectingView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setUpViews];
  }
  return self;
}
- (void)setUpViews {
  [self addSubview:self.backGroundImageView];
  [self setScene:YES];
  [self.backGroundImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.left.right.bottom.equalTo(self);
  }];

  [self addSubview:self.whiteCircleView];
  [self.whiteCircleView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self).offset(113);
    make.centerX.equalTo(self);
    make.height.width.equalTo(@136);
  }];

  [self addSubview:self.remoteImageView];
  [self.remoteImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self).offset(121);
    make.centerX.equalTo(self);
    make.width.height.equalTo(@120);
  }];

  [self addSubview:self.remoteNameLabel];
  [self.remoteNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.whiteCircleView.mas_bottom).offset(16);
    make.height.equalTo(@26);
    make.left.equalTo(self).offset(20);
    make.right.equalTo(self).offset(-20);
  }];
  [self addSubview:self.connectingStatusLabel];
  [self.connectingStatusLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.remoteNameLabel.mas_bottom).offset(12);
    //        make.height.equalTo(@18);
    make.left.equalTo(self).offset(20);
    make.right.equalTo(self).offset(-20);
  }];

  [self addSubview:self.connectingStatusDescLabel];
  [self.connectingStatusDescLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.connectingStatusLabel.mas_bottom).offset(24);
    //        make.height.equalTo(@22);
    make.left.equalTo(self).offset(20);
    make.right.equalTo(self).offset(-20);
  }];

  /// 发起方 取消按钮
  [self addSubview:self.cancelView];
  [self.cancelView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.equalTo(self);
    make.bottom.equalTo(self.mas_bottom).offset(-86);
    make.width.equalTo(@75);
    make.height.equalTo(@103);
  }];

  __weak typeof(self) weakSelf = self;
  self.cancelView.buttonClicked = ^{
    if (weakSelf.itemEvent) {
      weakSelf.itemEvent(item_cancel);
    }
  };

  /// 受邀方 拒绝按钮
  [self addSubview:self.rejectView];
  [self.rejectView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(56);
    make.bottom.equalTo(self.mas_bottom).offset(-86);
    make.width.equalTo(@75);
    make.height.equalTo(@103);
  }];

  self.rejectView.buttonClicked = ^{
    if (weakSelf.itemEvent) {
      weakSelf.itemEvent(item_reject);
    }
  };
  /// 受邀方 统一按钮
  [self addSubview:self.acceptView];
  [self.acceptView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self).offset(-56);
    make.bottom.equalTo(self.mas_bottom).offset(-86);
    make.width.equalTo(@75);
    make.height.equalTo(@103);
  }];

  self.acceptView.buttonClicked = ^{
    if (weakSelf.itemEvent) {
      weakSelf.itemEvent(item_accept);
    }
  };
  /// 通话中
  [self addSubview:self.audioButtomView];
  [self.audioButtomView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(75);
    make.right.equalTo(self).offset(-75);
    make.bottom.equalTo(self).offset(-98);
    make.height.equalTo(@60);
  }];

  self.audioButtomView.clickItem = ^(ButtonItemType item, BOOL close) {
    [weakSelf audioBottomItemClick:item close:close];
  };
}

- (void)startAudioCall {
}
- (void)setScene:(BOOL)isAudio {
  if (isAudio) {
    _backGroundImageView.image = [NEOneOnOneUI ne_imageName:@"call_audio_bg_icon"];
  } else {
    _backGroundImageView.image = [NEOneOnOneUI ne_imageName:@"call_video_bg_icon"];
  }
}
#pragma mark Lazy load
- (UIImageView *)backGroundImageView {
  if (!_backGroundImageView) {
    _backGroundImageView = [[UIImageView alloc] init];
  }
  return _backGroundImageView;
}

- (UIImageView *)remoteImageView {
  if (!_remoteImageView) {
    _remoteImageView = [[UIImageView alloc] init];
    _remoteImageView.layer.masksToBounds = YES;
    _remoteImageView.layer.cornerRadius = 60;
    _remoteImageView.backgroundColor = [UIColor redColor];
  }
  return _remoteImageView;
}
- (UIView *)whiteCircleView {
  if (!_whiteCircleView) {
    _whiteCircleView = [[UIView alloc] init];
    _whiteCircleView.backgroundColor = [UIColor clearColor];
    _whiteCircleView.layer.masksToBounds = YES;
    _whiteCircleView.layer.cornerRadius = 68;
    _whiteCircleView.layer.borderColor = [UIColor ne_colorWithHex:0xFFFFFF alpha:0.3].CGColor;
    _whiteCircleView.layer.borderWidth = 1;
  }
  return _whiteCircleView;
}

- (UILabel *)remoteNameLabel {
  if (!_remoteNameLabel) {
    _remoteNameLabel = [[UILabel alloc] init];
    _remoteNameLabel.font = [UIFont systemFontOfSize:24];
    _remoteNameLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    _remoteNameLabel.textAlignment = NSTextAlignmentCenter;
    _remoteNameLabel.text = @"Remote Name";
  }
  return _remoteNameLabel;
}
- (UILabel *)connectingStatusLabel {
  if (!_connectingStatusLabel) {
    _connectingStatusLabel = [[UILabel alloc] init];
    _connectingStatusLabel.textAlignment = NSTextAlignmentCenter;
    _connectingStatusLabel.numberOfLines = 0;
    _connectingStatusLabel.lineBreakMode = NSLineBreakByWordWrapping;
    _connectingStatusLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF alpha:0.55];
    _connectingStatusLabel.font = [UIFont systemFontOfSize:16];
  }
  return _connectingStatusLabel;
}

- (UILabel *)connectingStatusDescLabel {
  if (!_connectingStatusDescLabel) {
    _connectingStatusDescLabel = [[UILabel alloc] init];
    _connectingStatusDescLabel.textAlignment = NSTextAlignmentCenter;
    _connectingStatusDescLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    _connectingStatusDescLabel.font = [UIFont systemFontOfSize:16];
  }
  return _connectingStatusDescLabel;
}
- (NEOneOnOneCustomButtonView *)cancelView {
  if (!_cancelView) {
    _cancelView = [[NEOneOnOneCustomButtonView alloc] init];
    [_cancelView setButtonImage:@"reject_icon"];
    [_cancelView setTextLabel:NELocalizedString(@"取消")];
  }
  return _cancelView;
}

- (NEOneOnOneCustomButtonView *)rejectView {
  if (!_rejectView) {
    _rejectView = [[NEOneOnOneCustomButtonView alloc] init];
    [_rejectView setButtonImage:@"reject_icon"];
    [_rejectView setTextLabel:NELocalizedString(@"拒绝")];
  }
  return _rejectView;
}
- (NEOneOnOneCustomButtonView *)acceptView {
  if (!_acceptView) {
    _acceptView = [[NEOneOnOneCustomButtonView alloc] init];
    [_acceptView setButtonImage:@"accept_icon"];
    [_acceptView setTextLabel:NELocalizedString(@"接听")];
  }
  return _acceptView;
}
- (NEOneOnOneAudioButtomView *)audioButtomView {
  if (!_audioButtomView) {
    _audioButtomView = [[NEOneOnOneAudioButtomView alloc] init];
  }
  return _audioButtomView;
}

#pragma mark NEOneOnOneAudioButtomView action

- (void)audioBottomItemClick:(ButtonItemType)type close:(BOOL)close {
  switch (type) {
    case button_cancel:
      if (self.itemEvent) {
        self.itemEvent(item_close);
      }
      break;
    case button_mic:
      if (self.itemExpand) {
        self.itemExpand(item_mic, close);
      }
      break;
    case button_speaker:
      if (self.itemExpand) {
        self.itemExpand(item_speaker, close);
      }
      break;

    default:
      break;
  }
}

#pragma mark UI刷新
- (void)updateUI:(NSString *)remoteIcon
      remoteName:(NSString *)name
          status:(NECallViewStatus)status {
  __weak typeof(self) weakSelf = self;
  dispatch_async(dispatch_get_main_queue(), ^{
    __strong typeof(weakSelf) self = weakSelf;
    [self.remoteImageView sd_setImageWithURL:[NSURL URLWithString:remoteIcon]];
    self.remoteNameLabel.text = name;
    switch (status) {
        // 开始视频呼叫
      case video_call_start: {
        // 开始音频呼叫
        self.rejectView.hidden = YES;
        self.acceptView.hidden = YES;
        self.audioButtomView.hidden = YES;
        self.cancelView.hidden = NO;
        self.connectingStatusLabel.text = NELocalizedString(@"正在连接对方...");
        self.connectingStatusDescLabel.text = NELocalizedString(@"每个人都会遇见那个TA");
        [self setScene:NO];
        [self.acceptView setButtonImage:@"video_accept_icon"];
      } break;
      case audio_call_start: {
        // 开始音频呼叫
        self.rejectView.hidden = YES;
        self.acceptView.hidden = YES;
        self.audioButtomView.hidden = YES;
        self.cancelView.hidden = NO;
        self.connectingStatusLabel.text = NELocalizedString(@"正在连接对方...");
        self.connectingStatusDescLabel.text = NELocalizedString(@"每个人都会遇见那个TA");
        [self setScene:YES];
        [self.acceptView setButtonImage:@"accept_icon"];
      }

      break;
      case audio_invide_start: {
        // 被邀请音频通话
        self.rejectView.hidden = NO;
        self.acceptView.hidden = NO;
        self.audioButtomView.hidden = YES;
        self.cancelView.hidden = YES;
        self.connectingStatusLabel.text =
            NELocalizedString(@"对方邀请您语音聊天\n和对方进行开心之旅");
        self.connectingStatusDescLabel.text = @"";
        [self setScene:YES];
        [self.acceptView setButtonImage:@"accept_icon"];
      }

      break;
      case video_invite_start: {
        // 被邀请视频频通话
        self.rejectView.hidden = NO;
        self.acceptView.hidden = NO;
        self.audioButtomView.hidden = YES;
        self.cancelView.hidden = YES;
        self.connectingStatusLabel.text =
            NELocalizedString(@"对方邀请您视频聊天\n和对方进行开心之旅");
        self.connectingStatusDescLabel.text = @"";
        [self setScene:NO];
        [self.acceptView setButtonImage:@"video_accept_icon"];

      }

      break;
      case audio_call_connecting: {
        // 音频呼叫接通
        self.rejectView.hidden = YES;
        self.acceptView.hidden = YES;
        self.audioButtomView.hidden = NO;
        self.cancelView.hidden = YES;
        self.connectingStatusLabel.text = NELocalizedString(@"聊天中");

      }

      break;

      default:
        break;
    }
  });
}

/// 刷新定时器计数
- (void)updateTimer:(NSString *)time {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.connectingStatusDescLabel.text = time;
  });
}
@end
