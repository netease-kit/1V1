// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEOneOnOneRoomListViewModel.h"

@interface NEOneOnOneRoomListViewModel ()
@property(nonatomic, strong, readwrite) NSArray<NEOneOnOneOnlineUser *> *datas;
@property(nonatomic, assign, readwrite) BOOL isEnd;
@property(nonatomic, assign, readwrite) BOOL isLoading;
@property(nonatomic, strong, readwrite) NSError *error;

@property(nonatomic, assign) int32_t pageNum;
@property(nonatomic, assign) int32_t pageSize;
@end

@implementation NEOneOnOneRoomListViewModel

- (instancetype)init {
  self = [super init];
  if (self) {
    _pageNum = 0;
    _pageSize = 20;
    _datas = @[];
  }
  return self;
}

- (void)requestNewDataWithLiveType:(NEOneOnOneLiveRoomType)roomType {
  self.isLoading = YES;
  [[NEOneOnOneKit getInstance] getOneOnOneList:self.pageNum
                                      pageSize:20
                                      callback:^(NSInteger code, NSString *_Nullable msg,
                                                 NEOneOnOneOnlineUserList *_Nullable data) {
                                        dispatch_async(dispatch_get_main_queue(), ^{
                                          if (code != 0) {
                                            self.datas = @[];
                                            self.error = [NSError
                                                errorWithDomain:NSCocoaErrorDomain
                                                           code:code
                                                       userInfo:@{NSLocalizedDescriptionKey : msg}];
                                            self.isEnd = YES;
                                          } else {
                                            self.datas = data.data;
                                            self.error = nil;
                                            self.isEnd = ([data.data count] < self.pageSize);
                                          }
                                          self.isLoading = NO;
                                        });
                                      }];
}

// 加载更多
- (void)requestMoreDataWithLiveType:(NEOneOnOneLiveRoomType)roomType {
  if (_isEnd) {
    return;
  }
  self.isLoading = YES;
  self.pageNum += 1;
  [[NEOneOnOneKit getInstance]
      getOneOnOneList:self.pageNum
             pageSize:20
             callback:^(NSInteger code, NSString *_Nullable msg,
                        NEOneOnOneOnlineUserList *_Nullable data) {
               dispatch_async(dispatch_get_main_queue(), ^{
                 if (code != 0) {
                   self.datas = @[];
                   self.error = [NSError errorWithDomain:NSCocoaErrorDomain
                                                    code:code
                                                userInfo:@{NSLocalizedDescriptionKey : msg}];
                   self.isEnd = YES;
                 } else {
                   NSMutableArray *temp = [NSMutableArray arrayWithArray:self.datas];
                   [temp addObjectsFromArray:data.data];
                   self.datas = [temp copy];
                   self.isEnd = ([data.data count] < self.pageSize);
                   self.error = nil;
                 }
                 self.isLoading = NO;
               });
             }];
}

- (void)setDatas:(NSArray<NEOneOnOneOnlineUser *> *)datas {
  _datas = datas;
  if (_datasChanged) {
    _datasChanged(datas);
  }
}

- (void)setIsLoading:(BOOL)isLoading {
  _isLoading = isLoading;
  if (_isLoadingChanged) {
    _isLoadingChanged(isLoading);
  }
}

- (void)setError:(NSError *)error {
  _error = error;
  if (_errorChanged) {
    _errorChanged(error);
  }
}

@end
