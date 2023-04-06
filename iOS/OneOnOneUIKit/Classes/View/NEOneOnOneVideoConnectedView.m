// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneVideoConnectedView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import <SDWebImage/SDWebImage.h>
#import <libextobjc/extobjc.h>
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneVideoButtomView.h"
#import "NEOneonOneUI.h"
@interface NEOneOnOneVideoConnectedView ()

@property(nonatomic, strong) UIImageView *iconImageView;
@property(nonatomic, strong) NEOneOnOneVideoButtomView *videoButtomView;
@property(nonatomic, strong) UILabel *nameLabel;
@property(nonatomic, strong) UILabel *timerLabel;

/// 大小图点击切换按钮
@property(nonatomic, strong) UIButton *videoChangeButton;
/// 关闭视频按钮视图
@property(nonatomic, strong) UIView *videoCloseView;
@property(nonatomic, strong) UIButton *videoClose;
/// 小图关闭视频黑色视图
@property(nonatomic, strong) UIView *localVideoBlackView;
/// 小图关闭视频黑色视图文案
@property(nonatomic, strong) UILabel *localVideoCloseLabel;

/// 大图关闭视频黑色视图
@property(nonatomic, strong) UIView *remoteVideoBlackView;
/// 大图关闭视频黑色视图文案
@property(nonatomic, strong) UILabel *remoteVideoCloseLabel;
@property(nonatomic, strong) UIImageView *remoteVideoImageView;
/// 小图高斯模糊
@property(nonatomic, strong) UIVisualEffectView *smallEffectView;
/// 大图高斯模糊
@property(nonatomic, strong) UIVisualEffectView *largeEffectView;

@end
@implementation NEOneOnOneVideoConnectedView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self loadSubviews];
  }
  return self;
}

- (void)loadSubviews {
  [self addSubview:self.remoteVideoView];
  [self.remoteVideoView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.height.equalTo(self);
  }];

  /// 远端视频关闭
  [self addSubview:self.remoteVideoBlackView];
  [self.remoteVideoBlackView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.height.equalTo(self);
  }];
  [self.remoteVideoBlackView addSubview:self.remoteVideoImageView];
  [self.remoteVideoImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.height.equalTo(self.remoteVideoBlackView);
  }];
  [self.remoteVideoBlackView addSubview:self.remoteVideoCloseLabel];
  [self.remoteVideoCloseLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.center.equalTo(self.remoteVideoBlackView);
  }];

  [self addSubview:self.largeEffectView];
  [self.largeEffectView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.height.equalTo(self);
  }];
  self.largeEffectView.hidden = YES;

  self.remoteVideoBlackView.hidden = YES;

  [self addSubview:self.localVideoView];
  [self.localVideoView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.equalTo(@160);
    make.width.equalTo(@90);
    make.right.equalTo(self).offset(-12);
    make.top.equalTo(self).offset(56);
  }];

  /// 本端视频关闭
  [self addSubview:self.localVideoBlackView];
  [self.localVideoBlackView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.bottom.equalTo(self.localVideoView);
  }];
  self.localVideoBlackView.hidden = YES;

  [self.localVideoBlackView addSubview:self.localVideoCloseLabel];
  [self.localVideoCloseLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.center.equalTo(self.localVideoBlackView);
  }];

  [self addSubview:self.videoChangeButton];
  [self.videoChangeButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.bottom.equalTo(self.localVideoView);
  }];

  [self addSubview:self.smallEffectView];
  [self.smallEffectView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.bottom.equalTo(self.localVideoView);
  }];
  self.smallEffectView.hidden = YES;
  /// 视频关闭按钮
  [self addSubview:self.videoCloseView];
  [self.videoCloseView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.bottom.equalTo(self.localVideoView);
    make.width.equalTo(@32);
    make.height.equalTo(@24);
  }];

  [self.videoCloseView addSubview:self.videoClose];
  [self.videoClose mas_makeConstraints:^(MASConstraintMaker *make) {
    make.center.equalTo(self.videoCloseView);
    make.width.equalTo(@16);
    make.height.equalTo(@16);
  }];

  @weakify(self) self.videoButtomView.clickItem = ^(ButtonItemType item, BOOL close) {
    @strongify(self)[self videoBottomItemClick:item close:close];
  };

  [self addSubview:self.iconImageView];
  [self.iconImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self).offset(20);
    make.top.equalTo(self).offset(56);
    make.height.width.equalTo(@60);
  }];

  [self addSubview:self.nameLabel];
  [self.nameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self).offset(62.5);
    make.left.equalTo(self.iconImageView.mas_right).offset(12);
    make.height.equalTo(@25);
  }];

  [self addSubview:self.timerLabel];
  [self.timerLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.nameLabel);
    make.top.equalTo(self.nameLabel.mas_bottom).offset(2);
    make.height.equalTo(@20);
  }];

  [self addSubview:self.videoButtomView];
  [self.videoButtomView mas_makeConstraints:^(MASConstraintMaker *make) {
    //        make.centerX.equalTo(self);
    make.left.right.equalTo(self);
    make.height.equalTo(@60);
    //        make.width.equalTo(@284);
    make.bottom.equalTo(self).offset(-98);
  }];

  [self layoutIfNeeded];  // 这句代码很重要，不能忘了
  UIBezierPath *path =
      [UIBezierPath bezierPathWithRoundedRect:self.videoCloseView.bounds
                            byRoundingCorners:UIRectCornerTopRight | UIRectCornerBottomLeft
                                  cornerRadii:CGSizeMake(8, 8)];
  CAShapeLayer *shapeLayer = [CAShapeLayer layer];
  shapeLayer.frame = self.videoCloseView.bounds;
  shapeLayer.path = path.CGPath;
  self.videoCloseView.layer.mask = shapeLayer;
}

- (void)itemEvent:(UIButton *)sender {
  sender.selected = !sender.selected;
  switch (sender.tag) {
    case 0: {
      /// 视频开关
      if (self.itemExpand) {
        self.itemExpand(item_video_close, sender.selected);
        if (sender.selected) {
          self.localVideoBlackView.hidden = NO;
        } else {
          self.localVideoBlackView.hidden = YES;
        }
      }
    } break;
    case 1: {
      /// 大小图切换
      if (self.itemExpand) {
        self.videoCloseView.hidden = sender.selected;
        BOOL localBlackViewShow = self.localVideoBlackView.hidden;
        self.localVideoBlackView.hidden = self.remoteVideoBlackView.hidden;
        self.remoteVideoBlackView.hidden = localBlackViewShow;
        //        BOOL remoteBlackViewShow = !self.remoteVideoBlackView.hidden;

        if (localBlackViewShow) {
        }
        if (sender.selected) {
          self.remoteVideoCloseLabel.text = NELocalizedString(@"已关闭摄像头");
          self.localVideoCloseLabel.text = NELocalizedString(@"对方已关闭摄像头");
        } else {
          self.localVideoCloseLabel.text = NELocalizedString(@"已关闭摄像头");
          self.remoteVideoCloseLabel.text = NELocalizedString(@"对方已关闭摄像头");
        }
        //                if(localBlackViewShow){
        //                    //本端视频关闭
        //                    if(sender.selected){
        //                        //视频切换
        //                        self.remoteVideoBlackView.hidden = NO;
        //                        self.localVideoBlackView.hidden = YES;
        //                        _remoteVideoCloseLabel.text = NELocalizedString(@"已关闭摄像头");
        //                    }else{
        //                        self.remoteVideoBlackView.hidden = YES;
        //                        self.localVideoBlackView.hidden = NO;
        //                        _localVideoCloseLabel.text = NELocalizedString(@"已关闭摄像头");
        //                    }
        //                }
        //                if(remoteBlackViewShow){
        //                    //对端视频关闭
        //                    if(sender.selected){
        //                        //视频切换
        //                        self.localVideoBlackView.hidden = NO;
        //                        self.remoteVideoBlackView.hidden = YES;
        //                        _localVideoCloseLabel.text =
        //                        NELocalizedString(@"对方已关闭摄像头");
        //                    }else{
        //                        self.localVideoBlackView.hidden = YES;
        //                        self.remoteVideoBlackView.hidden = NO;
        //                        _remoteVideoCloseLabel.text =
        //                        NELocalizedString(@"对方已关闭摄像头");
        //                    }
        //                }

        self.itemExpand(item_video_change, sender.selected);
      }
    } break;

    default:
      break;
  }
}

- (void)videoBottomItemClick:(ButtonItemType)type close:(BOOL)close {
  switch (type) {
    case button_cancel:
      if (self.itemExpand) {
        self.itemExpand(item_close, close);
      }
      break;
    case button_mic:
      if (self.itemExpand) {
        self.itemExpand(item_mic, close);
      }
      break;
    case button_switch_camera: {
      if (self.itemExpand) {
        self.itemExpand(item_switch_camera, close);
      }

    } break;
    case button_speaker:
      if (self.itemExpand) {
        self.itemExpand(item_speaker, close);
      }
      break;

    default:
      break;
  }
}

/// 根据状态 刷新UI
- (void)updateUI:(NSString *)remoteIcon remoteName:(NSString *)name {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.iconImageView sd_setImageWithURL:[NSURL URLWithString:remoteIcon]];
    self.nameLabel.text = name;
  });
}

/// 刷新定时器计数
- (void)updateTimer:(NSString *)time {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.timerLabel.text = time;
  });
}

/// 是否展示大图黑图
- (void)showRemoteBlackView:(BOOL)show {
  if (self.videoChangeButton.isSelected) {
    self.localVideoBlackView.hidden = !show;
  } else {
    self.remoteVideoBlackView.hidden = !show;
  }
}
/// 是否展示小图黑图
- (void)showLocalBlackView:(BOOL)show {
  if (self.videoChangeButton.isSelected) {
    self.remoteVideoBlackView.hidden = !show;
  } else {
    self.localVideoBlackView.hidden = !show;
  }
}

/// 小图添加高斯模糊
- (void)showSmallEffectView:(BOOL)show {
  if (self.videoChangeButton.isSelected) {
    self.largeEffectView.hidden = !show;
  } else {
    self.smallEffectView.hidden = !show;
  }
}
/// 大图添加高斯模糊
- (void)showLargeEffectView:(BOOL)show {
  if (self.videoChangeButton.isSelected) {
    self.smallEffectView.hidden = !show;
  } else {
    self.largeEffectView.hidden = !show;
  }
}
#pragma mark lazyload

- (UIImageView *)iconImageView {
  if (!_iconImageView) {
    _iconImageView = [[UIImageView alloc] init];
    _iconImageView.layer.masksToBounds = YES;
    _iconImageView.layer.cornerRadius = 30;
  }
  return _iconImageView;
}

- (NEOneOnOneVideoButtomView *)videoButtomView {
  if (!_videoButtomView) {
    _videoButtomView = [[NEOneOnOneVideoButtomView alloc] init];
  }
  return _videoButtomView;
}
- (UILabel *)nameLabel {
  if (!_nameLabel) {
    _nameLabel = [[UILabel alloc] init];
    _nameLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:18];

    _nameLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    _nameLabel.backgroundColor = [UIColor clearColor];
    _nameLabel.textAlignment = NSTextAlignmentLeft;
  }
  return _nameLabel;
}
- (UILabel *)timerLabel {
  if (!_timerLabel) {
    _timerLabel = [[UILabel alloc] init];
    _timerLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF alpha:0.8];
    _timerLabel.backgroundColor = [UIColor clearColor];
    _timerLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
    _timerLabel.font = [UIFont systemFontOfSize:14];
    _timerLabel.textAlignment = NSTextAlignmentLeft;
  }
  return _timerLabel;
}
- (UIView *)localVideoView {
  if (!_localVideoView) {
    _localVideoView = [[UIView alloc] init];
    _localVideoView.layer.masksToBounds = YES;
    _localVideoView.layer.cornerRadius = 8;
  }
  return _localVideoView;
}

- (UIView *)remoteVideoView {
  if (!_remoteVideoView) {
    _remoteVideoView = [[UIView alloc] init];
  }
  return _remoteVideoView;
}

- (UIButton *)videoClose {
  if (!_videoClose) {
    _videoClose = [[UIButton alloc] init];
    _videoClose.selected = NO;
    _videoClose.contentMode = UIViewContentModeScaleAspectFit;
    [_videoClose setBackgroundImage:[NEOneOnOneUI ne_imageName:@"video_on_icon"]
                           forState:UIControlStateNormal];
    [_videoClose setBackgroundImage:[NEOneOnOneUI ne_imageName:@"video_off_icon"]
                           forState:UIControlStateSelected];
    [_videoClose addTarget:self
                    action:@selector(itemEvent:)
          forControlEvents:UIControlEventTouchUpInside];
    _videoClose.tag = 0;
  }
  return _videoClose;
}
- (UIView *)videoCloseView {
  if (!_videoCloseView) {
    _videoCloseView = [[UIView alloc] init];
    _videoCloseView.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.4];
    //        _videoCloseView.layer.masksToBounds = YES;
    //        _videoCloseView.layer.cornerRadius = 8;
  }
  return _videoCloseView;
}

- (UIView *)localVideoBlackView {
  if (!_localVideoBlackView) {
    _localVideoBlackView = [[UIView alloc] init];
    _localVideoBlackView.backgroundColor = [UIColor ne_colorWithHex:0x1D2026];
    _localVideoBlackView.layer.masksToBounds = YES;
    _localVideoBlackView.layer.cornerRadius = 8;
  }
  return _localVideoBlackView;
}

- (UILabel *)localVideoCloseLabel {
  if (!_localVideoCloseLabel) {
    _localVideoCloseLabel = [[UILabel alloc] init];
    _localVideoCloseLabel.textColor = [UIColor ne_colorWithHex:0x666666];
    _localVideoCloseLabel.text = NELocalizedString(@"已关闭摄像头");
    _localVideoCloseLabel.font = [UIFont systemFontOfSize:12];
    _localVideoCloseLabel.backgroundColor = [UIColor clearColor];
  }
  return _localVideoCloseLabel;
}

- (UIView *)remoteVideoBlackView {
  if (!_remoteVideoBlackView) {
    _remoteVideoBlackView = [[UIView alloc] init];
    _remoteVideoBlackView.backgroundColor = [UIColor clearColor];
  }
  return _remoteVideoBlackView;
}

- (UILabel *)remoteVideoCloseLabel {
  if (!_remoteVideoCloseLabel) {
    _remoteVideoCloseLabel = [[UILabel alloc] init];
    _remoteVideoCloseLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF alpha:0.7];
    _remoteVideoCloseLabel.text = NELocalizedString(@"对方已关闭摄像头");
    _remoteVideoCloseLabel.font = [UIFont systemFontOfSize:16];
    _remoteVideoCloseLabel.backgroundColor = [UIColor clearColor];
  }
  return _remoteVideoCloseLabel;
}
- (UIImageView *)remoteVideoImageView {
  if (!_remoteVideoImageView) {
    _remoteVideoImageView = [[UIImageView alloc] init];
    _remoteVideoImageView.image = [NEOneOnOneUI ne_imageName:@"remote_video_close_bg"];
  }
  return _remoteVideoImageView;
}

- (UIButton *)videoChangeButton {
  if (!_videoChangeButton) {
    _videoChangeButton = [[UIButton alloc] init];
    _videoChangeButton.selected = NO;
    [_videoChangeButton addTarget:self
                           action:@selector(itemEvent:)
                 forControlEvents:UIControlEventTouchUpInside];
    _videoChangeButton.tag = 1;
  }
  return _videoChangeButton;
}

- (UIVisualEffectView *)smallEffectView {
  if (!_smallEffectView) {
    UIBlurEffect *effect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleDark];
    _smallEffectView = [[UIVisualEffectView alloc] initWithEffect:effect];
    _smallEffectView.backgroundColor = [UIColor colorWithRed:0.192
                                                       green:0.239
                                                        blue:0.235
                                                       alpha:0.5];
    _smallEffectView.layer.masksToBounds = YES;
    _smallEffectView.layer.cornerRadius = 8;
  }
  return _smallEffectView;
}

- (UIVisualEffectView *)largeEffectView {
  if (!_largeEffectView) {
    UIBlurEffect *effect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleDark];
    _largeEffectView = [[UIVisualEffectView alloc] initWithEffect:effect];
    _largeEffectView.backgroundColor = [UIColor colorWithRed:0.192
                                                       green:0.239
                                                        blue:0.235
                                                       alpha:0.5];
  }
  return _largeEffectView;
}

- (void)updateVideoView:(UIButton *)sender {
  if (sender.selected) {
    [self.remoteVideoView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.height.equalTo(@160);
      make.width.equalTo(@90);
      make.right.equalTo(self).offset(-12);
      make.top.equalTo(self).offset(56);
    }];
    [self.remoteVideoView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.left.right.top.height.equalTo(self);
    }];

  } else {
    [self.localVideoView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.height.equalTo(@160);
      make.width.equalTo(@90);
      make.right.equalTo(self).offset(-12);
      make.top.equalTo(self).offset(56);
    }];
    [self.remoteVideoView mas_makeConstraints:^(MASConstraintMaker *make) {
      make.left.right.top.height.equalTo(self);
    }];
  }
}
@end
