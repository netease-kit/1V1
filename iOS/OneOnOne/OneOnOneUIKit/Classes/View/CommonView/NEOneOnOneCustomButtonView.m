// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneCustomButtonView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEOneOnOneUI.h"
@interface NEOneOnOneCustomButtonView ()

@end

@implementation NEOneOnOneCustomButtonView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setSubviews];
  }
  return self;
}

- (void)setSubviews {
  [self addSubview:self.customButton];
  [self.customButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.centerX.centerY.equalTo(self);
    make.height.width.equalTo(@75);
  }];

  [self addSubview:self.customLabel];
  [self.customLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.customButton.mas_bottom).offset(8);
    make.centerX.equalTo(self);
  }];
}
- (UIButton *)customButton {
  if (!_customButton) {
    _customButton = [[UIButton alloc] init];
    [_customButton addTarget:self
                      action:@selector(buttonClicked:)
            forControlEvents:UIControlEventTouchUpInside];
  }
  return _customButton;
}

- (void)buttonClicked:(UIButton *)sender {
  if (self.buttonClicked) {
    self.buttonClicked();
  }
}

- (UILabel *)customLabel {
  if (!_customLabel) {
    _customLabel = [[UILabel alloc] init];
    _customLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    _customLabel.font = [UIFont systemFontOfSize:14];
  }

  return _customLabel;
}
- (void)setTextLabel:(NSString *)text {
  self.customLabel.text = text;
}
// 设置按钮图片
- (void)setButtonImage:(NSString *)imageName {
  [self.customButton setBackgroundImage:[NEOneOnOneUI ne_imageName:imageName]
                               forState:UIControlStateNormal];
}
@end
