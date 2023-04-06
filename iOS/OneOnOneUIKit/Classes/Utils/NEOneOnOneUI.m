// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneUI.h"

@implementation NEOneOnOneUI
+ (UIImage *)ne_imageName:(NSString *)imageName {
  NSString *path = [[NSBundle bundleForClass:self.class].resourcePath
      stringByAppendingPathComponent:@"NEOneOnOneUIKit.bundle"];

  NSBundle *bundle = [NSBundle bundleWithPath:path];
  if (@available(iOS 13.0, *)) {
    UIImage *image = [UIImage imageNamed:imageName inBundle:bundle withConfiguration:nil];
    return image;
  }
  return [UIImage imageNamed:imageName inBundle:bundle compatibleWithTraitCollection:nil];
}
+ (NSBundle *_Nullable)ne_sourceBundle {
  NSString *path = [[NSBundle bundleForClass:self.class].resourcePath
      stringByAppendingPathComponent:@"NEOneOnOneUIKit.bundle"];

  NSBundle *bundle = [NSBundle bundleWithPath:path];
  return bundle;
}

/// 边距
+ (CGFloat)margin {
  return 30.0;
}
/// 麦位水平间距
+ (CGFloat)seatItemSpace {
  return 30.0;
}
/// 麦位垂直间距
+ (CGFloat)seatLineSpace {
  return 10.0;
}
@end
