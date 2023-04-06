// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneBottomPresentView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneUI.h"

@interface NEOneOnOneBottomPresentView ()
@property(nonatomic, strong) UIView *bottomBackView;
@property(nonatomic, strong) UIButton *audioButton;
@property(nonatomic, strong) UIButton *videoButton;
@property(nonatomic, assign) float showYPositon;
@property(nonatomic, strong) UIView *backView;

@end

@implementation NEOneOnOneBottomPresentView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:CGRectMake(frame.origin.x, frame.size.height, frame.size.width,
                                         frame.size.height)];
  if (self) {
    self.showYPositon = frame.origin.y;
    [self loadSubviews];
  }
  return self;
}

- (void)loadSubviews {
  [self layoutIfNeeded];
  [self addSubview:self.backView];
  self.backView.alpha = 0;
  [self addSubview:self.bottomBackView];
  [self addSubview:self.audioButton];
  [self addSubview:self.videoButton];

  [self.backView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.top.bottom.equalTo(self);
  }];
  [self.audioButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self.mas_bottom).offset(-34);
    make.left.equalTo(self).offset(23);
    make.height.equalTo(@44);
    float width = (self.frame.size.width - 23 * 2 - 15) / 2;
    make.width.equalTo(@(width));
  }];
  [self.videoButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self.mas_bottom).offset(-34);
    make.right.equalTo(self).offset(-23);
    make.height.equalTo(@44);
    float width = (self.frame.size.width - 23 * 2 - 15) / 2;
    make.width.equalTo(@(width));
  }];
  [self.bottomBackView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.bottom.equalTo(self);
    make.height.equalTo(@94);
  }];
}

- (UIButton *)audioButton {
  if (!_audioButton) {
    _audioButton = [[UIButton alloc] init];
    [_audioButton setImage:[NEOneOnOneUI ne_imageName:@"mic_icon"] forState:UIControlStateNormal];
    [_audioButton setImage:[NEOneOnOneUI ne_imageName:@"mic_icon"]
                  forState:UIControlStateHighlighted];
    [_audioButton setTitle:NELocalizedString(@"语音聊天") forState:UIControlStateNormal];
    _audioButton.titleLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    [_audioButton setTintColor:[UIColor ne_colorWithHex:0xFFFFFF]];
    _audioButton.backgroundColor = [UIColor clearColor];
    _audioButton.layer.masksToBounds = YES;
    _audioButton.layer.cornerRadius = 22;
    _audioButton.titleLabel.font = [UIFont systemFontOfSize:14];

    CAGradientLayer *gradientLayer = [CAGradientLayer layer];
    float width = (self.frame.size.width - 23 * 2 - 15) / 2;
    gradientLayer.frame = CGRectMake(0, 0, width, 44);
    gradientLayer.startPoint = CGPointMake(0, 0);
    gradientLayer.endPoint = CGPointMake(1, 0);
    gradientLayer.locations = @[ @(0.5), @(1.0) ];  // 渐变点
    [gradientLayer setColors:@[
      (id)[[UIColor ne_colorWithHex:0x4CC69A] CGColor],
      (id)[[UIColor ne_colorWithHex:0x24BF88] CGColor]
    ]];  // 渐变数组
    _audioButton.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 0, 2);
    _audioButton.titleEdgeInsets = UIEdgeInsetsMake(0, 2, 0, 0);
    [_audioButton.layer insertSublayer:gradientLayer atIndex:0];
    [_audioButton bringSubviewToFront:_audioButton.imageView];
    [_audioButton addTarget:self
                     action:@selector(clickAudio:)
           forControlEvents:UIControlEventTouchUpInside];
  }
  return _audioButton;
}

- (UIButton *)videoButton {
  if (!_videoButton) {
    _videoButton = [[UIButton alloc] init];
    [_videoButton setImage:[NEOneOnOneUI ne_imageName:@"video_on_icon"]
                  forState:UIControlStateNormal];
    [_videoButton setImage:[NEOneOnOneUI ne_imageName:@"video_on_icon"]
                  forState:UIControlStateHighlighted];
    [_videoButton setTitle:NELocalizedString(@"视频聊天") forState:UIControlStateNormal];
    _videoButton.titleLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    [_videoButton setTintColor:[UIColor ne_colorWithHex:0xFFFFFF]];
    _videoButton.backgroundColor = [UIColor ne_colorWithHex:0x337EFF];
    _videoButton.layer.masksToBounds = YES;
    _videoButton.layer.cornerRadius = 22;
    _videoButton.titleLabel.font = [UIFont systemFontOfSize:14];

    _videoButton.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 0, 2);
    _videoButton.titleEdgeInsets = UIEdgeInsetsMake(0, 2, 0, 0);

    [_videoButton bringSubviewToFront:_videoButton.imageView];
    [_videoButton addTarget:self
                     action:@selector(clickVideo:)
           forControlEvents:UIControlEventTouchUpInside];
  }
  return _videoButton;
}
- (UIView *)bottomBackView {
  if (!_bottomBackView) {
    _bottomBackView = [[UIView alloc] init];
    _bottomBackView.backgroundColor = [UIColor whiteColor];
  }
  return _bottomBackView;
}
- (UIView *)backView {
  if (!_backView) {
    _backView = [[UIView alloc] init];
    _backView.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.3];
  }
  return _backView;
}

- (void)show:(void (^)(void))complete {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.frame = CGRectMake(self.frame.origin.x, self.showYPositon + 94, self.frame.size.width,
                            self.frame.size.height);
    [UIView animateWithDuration:0.3
        animations:^{
          self.frame = CGRectMake(self.frame.origin.x, self.showYPositon, self.frame.size.width,
                                  self.frame.size.height);
        }
        completion:^(BOOL finished) {
          self.backView.alpha = 1;
          if (complete) {
            complete();
          }
        }];
  });
}
- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
  [self dismiss:nil];
}
- (void)dismiss:(void (^)(void))complete {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.backView.alpha = 0;
    [UIView animateWithDuration:0.3
        animations:^{
          self.frame = CGRectMake(self.frame.origin.x, self.showYPositon + 94,
                                  self.frame.size.width, self.frame.size.height);
        }
        completion:^(BOOL finished) {
          self.frame = CGRectMake(self.frame.origin.x, self.frame.size.height,
                                  self.frame.size.width, self.frame.size.height);
          if (complete) {
            complete();
          }
        }];
  });
}

- (void)clickAudio:(UIButton *)sender {
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"OneOnOneAudioCall" object:nil]];
  if (self.clickAudioAction) {
    self.clickAudioAction();
  }
}
- (void)clickVideo:(UIButton *)sender {
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"OneOnOneVideoCall" object:nil]];
  if (self.clickVideoAction) {
    self.clickVideoAction();
  }
}
@end
