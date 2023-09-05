// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import <Foundation/Foundation.h>
#import <NEOneOnOneKit/NEOneOnOneKit-Swift.h>
NS_ASSUME_NONNULL_BEGIN

@interface NEOneOnOneRoomListViewModel : NSObject

/// 数据源集合
@property(nonatomic, strong, readonly) NSArray<NEOneOnOneOnlineUser *> *datas;
/// 是否结束
@property(nonatomic, assign, readonly) BOOL isEnd;
/// 是否正在加载
@property(nonatomic, assign, readonly) BOOL isLoading;
/// 加载error
@property(nonatomic, strong, readonly) NSError *error;

@property(nonatomic, copy) void (^datasChanged)(NSArray<NEOneOnOneOnlineUser *> *datas);

@property(nonatomic, copy) void (^isLoadingChanged)(BOOL isLoading);

@property(nonatomic, copy) void (^errorChanged)(NSError *error);

// 麦位组件
// 加载新数据
- (void)requestNewDataWithLiveType:(NEOneOnOneLiveRoomType)roomType;
// 加载更多
- (void)requestMoreDataWithLiveType:(NEOneOnOneLiveRoomType)roomType;

@end

NS_ASSUME_NONNULL_END
