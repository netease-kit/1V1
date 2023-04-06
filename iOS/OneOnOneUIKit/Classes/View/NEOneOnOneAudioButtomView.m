// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneAudioButtomView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEOneOnOneUI.h"

@interface NEOneOnOneAudioButtomView ()
@property(nonatomic, strong) UIView *backView;
@property(nonatomic, strong) UIButton *micButton;
@property(nonatomic, strong) UIButton *loudspeakerButton;
@property(nonatomic, strong) UIButton *closeButton;

@end
@implementation NEOneOnOneAudioButtomView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self loadSubviews];
  }
  return self;
}

- (void)loadSubviews {
  [self addSubview:self.backView];
  [self.backView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.centerY.equalTo(self);
    make.height.equalTo(@60);
    make.width.equalTo(@224);
  }];
  [self.backView addSubview:self.micButton];
  [self.micButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.backView).offset(36);
    make.centerY.equalTo(self.backView);
    make.height.width.equalTo(@24);
  }];

  [self.backView addSubview:self.loudspeakerButton];
  [self.loudspeakerButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.centerY.equalTo(self.backView);
    make.height.width.equalTo(@24);
  }];
  [self.backView addSubview:self.closeButton];
  [self.closeButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.backView).offset(-36);
    make.centerY.equalTo(self.backView);
    make.height.width.equalTo(@32);
  }];
}

#pragma mark lazy load

- (UIView *)backView {
  if (!_backView) {
    _backView = [[UIView alloc] init];
    _backView.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.4];
    _backView.layer.masksToBounds = YES;
    _backView.layer.cornerRadius = 30;
  }
  return _backView;
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
    _closeButton.tag = 2;
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
