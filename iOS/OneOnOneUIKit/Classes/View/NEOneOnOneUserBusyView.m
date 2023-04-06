// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUserBusyView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneUI.h"

@interface NEOneOnOneUserBusyView ()

@property(nonatomic, strong) UIView *backView;
@property(nonatomic, strong) UIView *whiteView;
@property(nonatomic, strong) UILabel *busyLabel;
@property(nonatomic, strong) UILabel *lineLabel;
@property(nonatomic, strong) UIButton *sureButton;

@end

@implementation NEOneOnOneUserBusyView

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self loadSubViews];
  }
  return self;
}

- (void)loadSubViews {
  [self addSubview:self.backView];
  [self.backView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.top.right.bottom.equalTo(self);
  }];

  [self addSubview:self.whiteView];
  [self.whiteView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.center.equalTo(self);
    make.width.equalTo(@238);
    make.height.equalTo(@134);
  }];

  [self.whiteView addSubview:self.busyLabel];
  [self.busyLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.top.equalTo(self.whiteView).offset(20);
    make.centerX.equalTo(self);
  }];

  [self.whiteView addSubview:self.sureButton];
  [self.sureButton mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.bottom.equalTo(self.whiteView);
    make.height.equalTo(@42);
  }];
  [self.whiteView addSubview:self.lineLabel];
  [self.lineLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.right.equalTo(self.whiteView);
    make.bottom.equalTo(self.sureButton.mas_top);
    make.height.equalTo(@1);
  }];
}

#pragma mark lazy load

- (UIView *)backView {
  if (!_backView) {
    _backView = [[UIView alloc] init];
    _backView.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.4];
  }
  return _backView;
}

- (UIView *)whiteView {
  if (!_whiteView) {
    _whiteView = [[UIView alloc] init];
    _whiteView.backgroundColor = [UIColor whiteColor];
    _whiteView.layer.masksToBounds = YES;
    _whiteView.layer.cornerRadius = 8;
  }
  return _whiteView;
}
- (UILabel *)busyLabel {
  if (!_busyLabel) {
    _busyLabel = [[UILabel alloc] init];
    _busyLabel.textColor = [UIColor ne_colorWithHex:0x222222];
    _busyLabel.font = [UIFont systemFontOfSize:17];
    _busyLabel.numberOfLines = 0;
    _busyLabel.text = NELocalizedString(@"对方正在忙碌中\n请稍后再试");
    _busyLabel.textAlignment = NSTextAlignmentCenter;
  }
  return _busyLabel;
}

- (UILabel *)lineLabel {
  if (!_lineLabel) {
    _lineLabel = [[UILabel alloc] init];
    _lineLabel.backgroundColor = [UIColor ne_colorWithHex:0x222222 alpha:0.2];
    _lineLabel.text = @"";
  }
  return _lineLabel;
}
- (UIButton *)sureButton {
  if (!_sureButton) {
    _sureButton = [[UIButton alloc] init];
    _sureButton.backgroundColor = [UIColor clearColor];
    [_sureButton setTitleColor:[UIColor ne_colorWithHex:0x337EFF] forState:UIControlStateNormal];
    [_sureButton setTitle:NELocalizedString(@"确定") forState:UIControlStateNormal];
    [_sureButton addTarget:self
                    action:@selector(clickSureButton)
          forControlEvents:UIControlEventTouchUpInside];
  }
  return _sureButton;
}
- (void)removeAlertView {
  [self removeFromSuperview];
}

- (void)clickSureButton {
  [self removeFromSuperview];
}
@end
