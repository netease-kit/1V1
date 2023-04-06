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
    make.centerX.centerY.equalTo(self);
    make.width.equalTo(@284);
    make.height.equalTo(@60);
  }];

  [self.backGrayView addSubview:self.micButton];
  [self.micButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.backGrayView).offset(36);
    make.height.width.equalTo(@24);
  }];

  [self.backGrayView addSubview:self.loudspeakerButton];
  [self.loudspeakerButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.micButton.mas_right).offset(37);
    make.height.width.equalTo(@24);
  }];
  [self.backGrayView addSubview:self.switchButton];
  [self.switchButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.left.equalTo(self.loudspeakerButton.mas_right).offset(37);
    make.height.width.equalTo(@24);
  }];
  [self addSubview:self.closeButton];
  [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerY.equalTo(self.backGrayView);
    make.right.equalTo(self.backGrayView).offset(-36);
    make.height.width.equalTo(@32);
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
    [_micButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"mic_open_icon"]
                          forState:UIControlStateNormal];
    [_micButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"mic_close_icon"]
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
    [_loudspeakerButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"speaker_open_icon"]
                                  forState:UIControlStateNormal];
    [_loudspeakerButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"speaker_close_icon"]
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
    [_switchButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"camera_icon"]
                             forState:UIControlStateNormal];
    [_switchButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"camera_icon"]
                             forState:UIControlStateSelected];
    [_switchButton addTarget:self
                      action:@selector(itemEvent:)
            forControlEvents:UIControlEventTouchUpInside];
    _switchButton.tag = 2;
  }
  return _switchButton;
}
- (UIButton *)closeButton {
  if (!_closeButton) {
    _closeButton = [[UIButton alloc] init];
    _closeButton.selected = NO;
    [_closeButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"reject_small_icon"]
                            forState:UIControlStateNormal];
    [_closeButton setBackgroundImage:[NEOneOnOneUI ne_imageName:@"reject_small_icon"]
                            forState:UIControlStateSelected];
    [_closeButton addTarget:self
                     action:@selector(itemEvent:)
           forControlEvents:UIControlEventTouchUpInside];
    _closeButton.tag = 3;
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
