// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneEmptyListView.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/NEUIKit.h>
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneUI.h"
@interface NEOneOnOneEmptyListView ()

@property(nonatomic, strong) UIImageView *imgView;
@property(nonatomic, strong) UILabel *tipLabel;

@end

@implementation NEOneOnOneEmptyListView

- (instancetype)initWithFrame:(CGRect)frame {
  CGRect rect = CGRectMake(frame.origin.x, frame.origin.y, 150, 156);
  self = [super initWithFrame:rect];
  if (self) {
    [self ntes_addSubviews];
  }
  return self;
}

- (void)ntes_addSubviews {
  [self addSubview:self.imgView];
  [self addSubview:self.tipLabel];
  [self.imgView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(244, 239));
    make.centerX.equalTo(self);
    make.top.equalTo(self).offset(-206);
  }];

  [self.tipLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.size.mas_equalTo(CGSizeMake(200, 44));
    make.centerX.equalTo(self);
    make.top.equalTo(self.imgView.mas_bottom).offset(10);
  }];
}

- (void)setTintColor:(UIColor *)tintColor {
  if (_tintColor == tintColor) {
    return;
  }
  //    self.tipLabel.textColor = tintColor;
}

#pragma mark - lazy load

- (UIImageView *)imgView {
  if (!_imgView) {
    _imgView = [[UIImageView alloc] init];
    _imgView.image = [NEOneOnOneUI ne_imageName:@"list_empty_bg_icon"];
  }
  return _imgView;
}

- (UILabel *)tipLabel {
  if (!_tipLabel) {
    _tipLabel = [[UILabel alloc] init];
    _tipLabel.font = [UIFont systemFontOfSize:13];
    _tipLabel.textColor = [UIColor ne_colorWithHex:0x999999];
    _tipLabel.textAlignment = NSTextAlignmentCenter;
    _tipLabel.text = NELocalizedString(@"还没有在线用户哦\n和你的好友一起来体验吧");
    _tipLabel.numberOfLines = 0;
  }
  return _tipLabel;
}

@end
