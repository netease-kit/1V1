// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#ifndef NEOneOnOneUIKitMacro_h
#define NEOneOnOneUIKitMacro_h

#define CALLER_USER_NAME @"caller_userName"
#define CALLER_USER_MOBILE @"caller_userMobile"
#define CALLER_USER_AVATAR @"caller_userAvatar"
#define CALLED_USER_NAME @"called_userName"
#define CALLED_USER_MOBILE @"called_userMobile"
#define CALLED_USER_AVATAR @"called_userAvatar"
#define PARAM_KEY_TITLE @"param_key_title"
#define PARAM_KEY_URL @"param_key_url"
#define INDEX @"index"

#define UIScreenWidth ([UIScreen mainScreen].bounds.size.width)
#define UIScreenHeight ([UIScreen mainScreen].bounds.size.height)

#define NELocalConnectingDuration @"ConnectionDuration"
#define NEOneOnOneCallViewControllerAppear @"NEOneOnOneCallViewControllerAppear"
#define NEOneOnOneCallViewControllerBusy @"NEOneOnOneCallViewControllerBusy"

typedef enum : NSUInteger {
  // 主动拨打音频
  audio_call,
  // 主动拨打视频
  video_call,
  // 收到音频邀请通知
  audio_invited,
  // 收到视频邀请通知
  video_invited,
  // 默认
  enter_default,
} NEEnterStatus;

/// 通话中 底部视图点击点击事件
typedef enum : NSUInteger {
  /// 麦克风
  button_mic,
  /// 扬声器
  button_speaker,
  /// 切换摄像头
  button_switch_camera,
  /// 关闭本地摄像头
  button_close_camera,
  /// 取消
  button_cancel,

} ButtonItemType;

/// 定义点击Block
typedef void (^ClickItem)(ButtonItemType item, BOOL close);

/// 通话中视图 封装 Event
typedef enum : NSUInteger {
  // 取消通话
  item_cancel,
  // 拒绝通话
  item_reject,
  // 接受通话
  item_accept,
  // 挂断电话
  item_close,
  // 麦克风点击事件
  item_mic,
  // 扬声器点击事件
  item_speaker,
  // 切换摄像头
  item_switch_camera,
  // 开关摄像头
  item_video_close,
  // 切换大小图
  item_video_change,
} Item;

/// 定义点击Block
typedef void (^ItemEvent)(Item item);
/// 定义点击Block
typedef void (^ItemEventExpand)(Item item, BOOL close);

// 进入控制器状态
typedef enum : NSUInteger {
  // 发起音频
  audio_call_start,
  // 被邀请音频
  audio_invide_start,
  // 发起视频
  video_call_start,
  // 被邀请视频
  video_invite_start,
  // 音频通话中
  audio_call_connecting,
  // 视频通话中
  video_call_connecting

} NECallViewStatus;

/// 用户状态
typedef enum : NSUInteger {
  in_voice_room,
  in_listen_together,
  in_default,
} NEUserState;

#endif /* NEOneOnOneUIKitMacro_h */
