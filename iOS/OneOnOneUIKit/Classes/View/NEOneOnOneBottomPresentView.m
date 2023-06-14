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
/// 私信按钮
@property(nonatomic, strong) UIButton *privateLatterButton;
/// 私信标签
@property(nonatomic, strong) UILabel *privateLatterLabel;

/// 语音按钮
@property(nonatomic, strong) UIButton *audioButton;
/// 语音标签
@property(nonatomic, strong) UILabel *audioLabel;

/// 视频按钮
@property(nonatomic, strong) UIButton *videoButton;
/// 视频标签
@property(nonatomic, strong) UILabel *videoLabel;

@property(nonatomic, strong) UIButton *chatUpButton;
//@property(nonatomic, strong) UIButton *audioButton;
//@property(nonatomic, strong) UIButton *videoButton;
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

  [self addSubview:self.chatUpButton];
  [self.chatUpButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.bottom.equalTo(self.mas_bottom).offset(-34);
    make.left.equalTo(self).offset(20);
    make.height.equalTo(@44);
    float width = self.frame.size.width / 2 - 10 * 2 - 15;
    make.width.equalTo(@(width));
  }];

  [self addSubview:self.privateLatterButton];
  [self.privateLatterButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.width.equalTo(@32);
    make.bottom.equalTo(self.mas_bottom).offset(-50);
    make.left.equalTo(self.mas_centerX).offset(13);
  }];

  [self addSubview:self.privateLatterLabel];
  [self.privateLatterLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.privateLatterButton.mas_bottom);
    make.centerX.equalTo(self.privateLatterButton.mas_centerX);
  }];

  [self addSubview:self.audioButton];
  [self.audioButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.width.equalTo(@32);
    make.centerY.equalTo(self.privateLatterButton.mas_centerY);
    make.left.equalTo(self.privateLatterButton.mas_right).offset(20);
  }];

  [self addSubview:self.audioLabel];
  [self.audioLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.audioButton.mas_bottom);
    make.centerX.equalTo(self.audioButton.mas_centerX);
  }];

  [self addSubview:self.videoButton];
  [self.videoButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.height.width.equalTo(@32);
    make.centerY.equalTo(self.privateLatterButton.mas_centerY);
    make.left.equalTo(self.audioButton.mas_right).offset(20);
  }];

  [self addSubview:self.videoLabel];
  [self.videoLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.videoButton.mas_bottom);
    make.centerX.equalTo(self.videoButton.mas_centerX);
  }];

  [self.bottomBackView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.bottom.equalTo(self);
    make.height.equalTo(@94);
  }];
}

//- (UIButton *)audioButton {
//  if (!_audioButton) {
//    _audioButton = [[UIButton alloc] init];
//    [_audioButton setImage:[NEOneOnOneUI ne_imageName:@"mic_icon"] forState:UIControlStateNormal];
//    [_audioButton setImage:[NEOneOnOneUI ne_imageName:@"mic_icon"]
//                  forState:UIControlStateHighlighted];
//    [_audioButton setTitle:NELocalizedString(@"语音聊天") forState:UIControlStateNormal];
//    _audioButton.titleLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
//    [_audioButton setTintColor:[UIColor ne_colorWithHex:0xFFFFFF]];
//    _audioButton.backgroundColor = [UIColor clearColor];
//    _audioButton.layer.masksToBounds = YES;
//    _audioButton.layer.cornerRadius = 22;
//    _audioButton.titleLabel.font = [UIFont systemFontOfSize:14];
//
//    CAGradientLayer *gradientLayer = [CAGradientLayer layer];
//    float width = (self.frame.size.width - 23 * 2 - 15) / 2;
//    gradientLayer.frame = CGRectMake(0, 0, width, 44);
//    gradientLayer.startPoint = CGPointMake(0, 0);
//    gradientLayer.endPoint = CGPointMake(1, 0);
//    gradientLayer.locations = @[ @(0.5), @(1.0) ];  // 渐变点
//    [gradientLayer setColors:@[
//      (id)[[UIColor ne_colorWithHex:0x4CC69A] CGColor],
//      (id)[[UIColor ne_colorWithHex:0x24BF88] CGColor]
//    ]];  // 渐变数组
//    _audioButton.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 0, 2);
//    _audioButton.titleEdgeInsets = UIEdgeInsetsMake(0, 2, 0, 0);
//    [_audioButton.layer insertSublayer:gradientLayer atIndex:0];
//    [_audioButton bringSubviewToFront:_audioButton.imageView];
//    [_audioButton addTarget:self
//                     action:@selector(clickAudio:)
//           forControlEvents:UIControlEventTouchUpInside];
//  }
//  return _audioButton;
//}

- (UIButton *)chatUpButton {
  if (!_chatUpButton) {
    _chatUpButton = [[UIButton alloc] init];
    [_chatUpButton setImage:[NEOneOnOneUI ne_imageName:@"chat_up_icon"]
                   forState:UIControlStateNormal];
    [_chatUpButton setImage:[NEOneOnOneUI ne_imageName:@"chat_up_icon"]
                   forState:UIControlStateHighlighted];
    [_chatUpButton setTitle:NELocalizedString(@"搭讪") forState:UIControlStateNormal];
    _chatUpButton.titleLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    [_chatUpButton setTintColor:[UIColor ne_colorWithHex:0xFFFFFF]];
    _chatUpButton.backgroundColor = [UIColor clearColor];
    _chatUpButton.layer.masksToBounds = YES;
    _chatUpButton.layer.cornerRadius = 22;
    _chatUpButton.titleLabel.font = [UIFont systemFontOfSize:14];
    CAGradientLayer *gradientLayer = [CAGradientLayer layer];
    float width = (self.frame.size.width - 23 * 2 - 15) / 2;
    gradientLayer.frame = CGRectMake(0, 0, width, 44);
    gradientLayer.startPoint = CGPointMake(0, 0);
    gradientLayer.endPoint = CGPointMake(1, 0);
    gradientLayer.locations = @[ @(0.5), @(1.0) ];  // 渐变点
    [gradientLayer setColors:@[
      (id)[[UIColor ne_colorWithHex:0xF9627C] CGColor],
      (id)[[UIColor ne_colorWithHex:0xFF8073] CGColor]
    ]];  // 渐变数组
    _chatUpButton.imageEdgeInsets = UIEdgeInsetsMake(0, 0, 0, 2);
    _chatUpButton.titleEdgeInsets = UIEdgeInsetsMake(0, 2, 0, 0);
    [_chatUpButton.layer insertSublayer:gradientLayer atIndex:0];
    [_chatUpButton bringSubviewToFront:_chatUpButton.imageView];
    [_chatUpButton addTarget:self
                      action:@selector(clickChatUp:)
            forControlEvents:UIControlEventTouchUpInside];
  }
  return _chatUpButton;
}

/// 私信
- (UIButton *)privateLatterButton {
  if (!_privateLatterButton) {
    _privateLatterButton = [[UIButton alloc] init];
    [_privateLatterButton setImage:[NEOneOnOneUI ne_imageName:@"private_letter_icon"]
                          forState:UIControlStateNormal];
    [_privateLatterButton setImage:[NEOneOnOneUI ne_imageName:@"private_letter_icon"]
                          forState:UIControlStateHighlighted];
    [_privateLatterButton addTarget:self
                             action:@selector(clickPrivateLatterButton:)
                   forControlEvents:UIControlEventTouchUpInside];
  }
  return _privateLatterButton;
}

- (UILabel *)privateLatterLabel {
  if (!_privateLatterLabel) {
    _privateLatterLabel = [[UILabel alloc] init];
    _privateLatterLabel.text = NELocalizedString(@"私信");
    _privateLatterLabel.font = [UIFont systemFontOfSize:10];
    _privateLatterLabel.textColor = [UIColor ne_colorWithHex:0x8E95A9];
  }
  return _privateLatterLabel;
}
/// 语音
- (UIButton *)audioButton {
  if (!_audioButton) {
    _audioButton = [[UIButton alloc] init];
    [_audioButton setImage:[NEOneOnOneUI ne_imageName:@"audio_call_icon"]
                  forState:UIControlStateNormal];
    [_audioButton setImage:[NEOneOnOneUI ne_imageName:@"audio_call_icon"]
                  forState:UIControlStateHighlighted];
    [_audioButton addTarget:self
                     action:@selector(clickAudio:)
           forControlEvents:UIControlEventTouchUpInside];
  }
  return _audioButton;
}

- (UILabel *)audioLabel {
  if (!_audioLabel) {
    _audioLabel = [[UILabel alloc] init];
    _audioLabel.text = NELocalizedString(@"语音");
    _audioLabel.font = [UIFont systemFontOfSize:10];
    _audioLabel.textColor = [UIColor ne_colorWithHex:0x8E95A9];
  }
  return _audioLabel;
}

/// 视频
- (UIButton *)videoButton {
  if (!_videoButton) {
    _videoButton = [[UIButton alloc] init];
    [_videoButton setImage:[NEOneOnOneUI ne_imageName:@"video_call_icon"]
                  forState:UIControlStateNormal];
    [_videoButton setImage:[NEOneOnOneUI ne_imageName:@"video_call_icon"]
                  forState:UIControlStateHighlighted];
    [_videoButton addTarget:self
                     action:@selector(clickVideo:)
           forControlEvents:UIControlEventTouchUpInside];
  }
  return _videoButton;
}

- (UILabel *)videoLabel {
  if (!_videoLabel) {
    _videoLabel = [[UILabel alloc] init];
    _videoLabel.text = NELocalizedString(@"视频");
    _videoLabel.font = [UIFont systemFontOfSize:10];
    _videoLabel.textColor = [UIColor ne_colorWithHex:0x8E95A9];
  }
  return _videoLabel;
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
      postNotification:[NSNotification notificationWithName:@"oneOnOneAudioCall" object:nil]];
  if (self.clickAudioAction) {
    self.clickAudioAction();
  }
}

- (void)clickChatUp:(UIButton *)sender {
  /// 埋点内容待修改
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"OneOnOneChatUp" object:nil]];
  if (self.clickChatUpAction) {
    self.clickChatUpAction();
  }
}
- (void)clickPrivateLatterButton:(UIButton *)sender {
  /// 埋点内容待修改
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"OneOnOnePrivateLetter" object:nil]];
  if (self.clickPrivateLatterAction) {
    self.clickPrivateLatterAction();
  }
}

- (void)clickVideo:(UIButton *)sender {
  [NSNotificationCenter.defaultCenter
      postNotification:[NSNotification notificationWithName:@"oneOnOneVideoCall" object:nil]];
  if (self.clickVideoAction) {
    self.clickVideoAction();
  }
}
@end
