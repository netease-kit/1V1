// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUILiveListCell.h"
#import <Masonry/Masonry.h>
#import <NEUIKit/UIColor+NEUIExtension.h>
#import <SDWebImage/SDWebImage.h>
#import "NEOneOnOneLocalized.h"
#import "NEOneOnOneUI.h"
#import "NEOneOnOneUIKitMacro.h"
#import "NEPaddingLabel.h"

@interface NEOneOnOneUILiveListCell ()

/// 封面
@property(nonatomic, strong) UIImageView *coverView;
/// 背景
@property(nonatomic, strong) UILabel *backLabel;
/// 在线
@property(nonatomic, strong) UILabel *onlineLabel;
/// 在线图标 小绿点
@property(nonatomic, strong) UIImageView *onlineImageView;
/// 房间描述
@property(nonatomic, strong) NEPaddingLabel *roomLabel;
@end

@implementation NEOneOnOneUILiveListCell

- (instancetype)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setupViews];
  }
  return self;
}

- (void)setupViews {
  [self.contentView addSubview:self.coverView];
  [self.contentView addSubview:self.backLabel];
  [self.contentView addSubview:self.onlineLabel];
  [self.contentView addSubview:self.onlineImageView];
  [self.contentView addSubview:self.roomLabel];

  [self.coverView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.edges.equalTo(self.contentView);
  }];

  [self.onlineLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.contentView).offset(-8);
    make.top.equalTo(self.contentView).offset(7);
    make.height.equalTo(@12);
  }];
  [self.onlineImageView mas_makeConstraints:^(MASConstraintMaker *make) {
    make.right.equalTo(self.onlineLabel.mas_left).offset(-4);
    make.centerY.equalTo(self.onlineLabel);
    make.width.height.equalTo(@4);
  }];
  [self.roomLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.contentView).offset(8);
    make.bottom.equalTo(self.contentView).offset(-8);
    make.right.lessThanOrEqualTo(self.contentView).offset(-8);
    make.height.equalTo(@20);
  }];

  [self.backLabel mas_makeConstraints:^(MASConstraintMaker *make) {
    make.left.equalTo(self.onlineImageView.mas_left).offset(-5);
    make.right.equalTo(self.onlineLabel.mas_right).offset(5);
    make.height.equalTo(@20);
    make.centerY.equalTo(self.onlineLabel);
  }];
}

- (void)installWithModel:(NEOneOnOneOnlineUser *)model indexPath:(NSIndexPath *)indexPath {
  [self.coverView sd_setImageWithURL:[NSURL URLWithString:model.icon]];
  self.roomLabel.text = [NSString stringWithFormat:@"%@", model.userName];
}

- (NSString *)praiseStrFormat:(NSUInteger)number {
  NSString *str;
  if (number == 0) {
    str = @"0";
  } else if (number > 0 && number <= 10000) {
    str = @(number).stringValue;
  } else {
    //        保留两位小数 不四舍五入
    str = [NSString stringWithFormat:@"%.2f", floor((number / 10000.0) * 100) / 100];
    //        保留两位小数 四舍五入
    str = [NSString stringWithFormat:@"%.1fw", (number / 10000.0)];
    ////        去除末尾0
    //        str = [NSString stringWithFormat:@"%@w",@(str.floatValue)];
  }
  return str;
}

+ (NEOneOnOneUILiveListCell *)cellWithCollectionView:(UICollectionView *)collectionView
                                           indexPath:(NSIndexPath *)indexPath
                                               datas:(NSArray<NEOneOnOneOnlineUser *> *)datas {
  NEOneOnOneUILiveListCell *cell =
      [collectionView dequeueReusableCellWithReuseIdentifier:[NEOneOnOneUILiveListCell description]
                                                forIndexPath:indexPath];
  if ([datas count] > indexPath.row) {
    NEOneOnOneOnlineUser *model = datas[indexPath.row];
    [cell installWithModel:model indexPath:indexPath];
  }
  return cell;
}

+ (CGSize)size {
  CGFloat length = (UIScreenWidth - 8 * 5) / 2.0;
  return CGSizeMake(length, length);
}

#pragma mark - lazy load

- (UIImageView *)coverView {
  if (!_coverView) {
    _coverView = [[UIImageView alloc] init];
    _coverView.contentMode = UIViewContentModeScaleAspectFill;
    _coverView.layer.cornerRadius = 4;
    _coverView.layer.masksToBounds = YES;
    _coverView.accessibilityIdentifier = @"id.iv";
  }
  return _coverView;
}

- (NEPaddingLabel *)roomLabel {
  if (!_roomLabel) {
    _roomLabel = [[NEPaddingLabel alloc] init];
    _roomLabel.edgeInsets = UIEdgeInsetsMake(5, 8, 4, 8);
    _roomLabel.font = [UIFont systemFontOfSize:10];
    _roomLabel.textColor = [UIColor whiteColor];
    _roomLabel.layer.masksToBounds = YES;
    _roomLabel.layer.cornerRadius = 2;
    _roomLabel.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.5];
    _roomLabel.text = @"房间描述";
    _roomLabel.accessibilityIdentifier = @"id.tv";
  }
  return _roomLabel;
}

- (UILabel *)onlineLabel {
  if (!_onlineLabel) {
    _onlineLabel = [[UILabel alloc] init];
    _onlineLabel.text = NELocalizedString(@"在线");
    _onlineLabel.textColor = [UIColor ne_colorWithHex:0xFFFFFF];
    _onlineLabel.font = [UIFont systemFontOfSize:10];
    _onlineLabel.accessibilityIdentifier = @"id.onlineState";
  }
  return _onlineLabel;
}
- (UILabel *)backLabel {
  if (!_backLabel) {
    _backLabel = [[UILabel alloc] init];
    _backLabel.text = @"";
    _backLabel.backgroundColor = [UIColor ne_colorWithHex:0x000000 alpha:0.5];
    _backLabel.layer.masksToBounds = YES;
    _backLabel.layer.cornerRadius = 10;
  }
  return _backLabel;
}

- (UIImageView *)onlineImageView {
  if (!_onlineImageView) {
    _onlineImageView = [[UIImageView alloc] init];
    _onlineImageView.image = [NEOneOnOneUI ne_imageName:@"online_icon"];
    _onlineImageView.contentMode = UIViewContentModeCenter;
  }
  return _onlineImageView;
}
@end
