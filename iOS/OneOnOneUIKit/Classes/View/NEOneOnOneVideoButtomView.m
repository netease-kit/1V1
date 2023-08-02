// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneVideoButtomView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEOneonOneUI.h"
@interface NEOneOnOneVideoButtomView ()
@property(nonatomic, strong) UIView *backGrayView;
@property(nonatomic, strong) UIButton *micButton;
@property(nonatomic, strong) UIButton *loudspeakerButton;
@property(nonatomic, strong) UIButton *switchButton;
@property(nonatomic, strong) UIButton *closeButton;
@property(nonatomic, strong) UIButton *videoCloseButton;

@end

@implementation NEOneOnOneVideoButtomView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self loadSubviews];
  }
  return self;
}

- (void)loadSubviews {
  [self addSubview:self.backGrayView];
  [self.backGrayView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self);
    make.left.equalTo(self).offset(15);
    make.right.equalTo(self).offset(-15);
    make.height.equalTo(@60);
  }];

  [self.backGrayView addSubview:self.micButton];
  [self.micButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.backGrayView).offset(36);
  }];

  [self.backGrayView addSubview:self.loudspeakerButton];
  [self.loudspeakerButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.micButton.mas_right);
    make.height.equalTo(self.micButton.mas_height);
    make.width.equalTo(self.micButton.mas_width);
  }];
  [self.backGrayView addSubview:self.switchButton];
  [self.switchButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.loudspeakerButton.mas_right);
    make.height.equalTo(self.micButton.mas_height);
    make.width.equalTo(self.micButton.mas_width);
  }];

  [self addSubview:self.videoCloseButton];
  [self.videoCloseButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.switchButton.mas_right);
    make.height.equalTo(self.micButton.mas_height);
    make.width.equalTo(self.micButton.mas_width);
  }];

  [self addSubview:self.closeButton];
  [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.right.equalTo(self.backGrayView).offset(-36);
    make.left.equalTo(self.videoCloseButton.mas_right);
    make.height.equalTo(self.micButton.mas_height);
    make.width.equalTo(self.micButton.mas_width);
  }];
}

#pragma mark lazyload
- (UIView *)backGrayView {
  if (!_backGrayView) {
    _backGrayView = [[UIView alloc] init];
    _backGrayView.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.4];
    _backGrayView.layer.masksToBounds = YES;
    _backGrayView.layer.cornerRadius = 30;
  }
  return _backGrayView;
}

- (UIButton *)micButton {
  if (!_micButton) {
    _micButton = [[UIButton alloc] init];
    _micButton.selected = NO;
    [_micButton setImage:[NEOneOnOneUI ne_imageName:@"mic_open_icon"]
                forState:UIControlStateNormal];
    [_micButton setImage:[NEOneOnOneUI ne_imageName:@"mic_close_icon"]
                forState:UIControlStateSelected];
    [_micButton addTarget:self
                   action:@selector(itemEvent:)
         forControlEvents:UIControlEventTouchUpInside];
    _micButton.tag = 0;
  }
  return _micButton;
}

- (UIButton *)loudspeakerButton {
  if (!_loudspeakerButton) {
    _loudspeakerButton = [[UIButton alloc] init];
    _loudspeakerButton.selected = NO;
    [_loudspeakerButton setImage:[NEOneOnOneUI ne_imageName:@"speaker_open_icon"]
                        forState:UIControlStateNormal];
    [_loudspeakerButton setImage:[NEOneOnOneUI ne_imageName:@"speaker_close_icon"]
                        forState:UIControlStateSelected];
    [_loudspeakerButton addTarget:self
                           action:@selector(itemEvent:)
                 forControlEvents:UIControlEventTouchUpInside];
    _loudspeakerButton.tag = 1;
  }
  return _loudspeakerButton;
}
- (UIButton *)switchButton {
  if (!_switchButton) {
    _switchButton = [[UIButton alloc] init];
    _switchButton.selected = NO;
    [_switchButton setImage:[NEOneOnOneUI ne_imageName:@"camera_icon"]
                   forState:UIControlStateNormal];
    [_switchButton setImage:[NEOneOnOneUI ne_imageName:@"camera_icon"]
                   forState:UIControlStateSelected];
    [_switchButton addTarget:self
                      action:@selector(itemEvent:)
            forControlEvents:UIControlEventTouchUpInside];
    _switchButton.tag = 2;
  }
  return _switchButton;
}

- (UIButton *)videoCloseButton {
  if (!_videoCloseButton) {
    _videoCloseButton = [[UIButton alloc] init];
    _videoCloseButton.selected = NO;
    [_videoCloseButton setImage:[NEOneOnOneUI ne_imageName:@"video_on_icon"]
                       forState:UIControlStateNormal];
    [_videoCloseButton setImage:[NEOneOnOneUI ne_imageName:@"video_off_icon"]
                       forState:UIControlStateSelected];
    [_videoCloseButton addTarget:self
                          action:@selector(itemEvent:)
                forControlEvents:UIControlEventTouchUpInside];
    _videoCloseButton.tag = 3;
  }
  return _videoCloseButton;
}
- (UIButton *)closeButton {
  if (!_closeButton) {
    _closeButton = [[UIButton alloc] init];
    _closeButton.selected = NO;
    [_closeButton setImage:[NEOneOnOneUI ne_imageName:@"reject_small_icon"]
                  forState:UIControlStateNormal];
    [_closeButton setImage:[NEOneOnOneUI ne_imageName:@"reject_small_icon"]
                  forState:UIControlStateSelected];
    [_closeButton addTarget:self
                     action:@selector(itemEvent:)
           forControlEvents:UIControlEventTouchUpInside];
    _closeButton.tag = 4;
  }
  return _closeButton;
}

- (void)itemEvent:(UIButton *)sender {
  sender.selected = !sender.selected;
  switch (sender.tag) {
    case 0: {
      /// 麦克风
      if (self.clickItem) {
        self.clickItem(button_mic, sender.selected);
      }
    } break;

    case 1: {
      /// 扬声器
      if (self.clickItem) {
        self.clickItem(button_speaker, sender.selected);
      }
    } break;

    case 2: {
      if (self.clickItem) {
        self.clickItem(button_switch_camera, sender.selected);
      }
    } break;
    case 3: {
      if (self.clickItem) {
        self.clickItem(button_close_camera, sender.selected);
      }
    } break;
    case 4: {
      /// 挂断
      if (self.clickItem) {
        self.clickItem(button_cancel, sender.selected);
      }
    } break;
    default:
      break;
  }
}
@end
