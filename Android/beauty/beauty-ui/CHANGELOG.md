# Common ChangeLog

## v1.0.6(February 08 2023)
### New Features
* NEVoiceRoomKit新增如下接口：
  - enableAudioVolumeIndication 启用说话者音量提示
  - sendBatchGift  批量礼物发送
* NEVoiceRoomListener新增如下接口：
  - onRtcAudioVolumeIndication 提示房间内谁正在说话及说话者瞬时音量的回调
  - onReceiveBatchGift 收到批量礼物回调
* NEVoiceRoomLiveModel 新增字段seatUserReward：打赏信息
* 新增NEVoiceRoomBatchSeatUserReward: 打赏详情
* 兼容 `NIM` 9.6.4 版本
* 兼容 `NERtc` 4.6.29 版本
* 兼容 `NERoom` 1.11.1 版本

## v1.0.5(January 13, 2023)
### BugFix
- fix海外环境初始化问题

* 兼容 `NIM` 9.6.4 版本
* 兼容 `NERtc` 4.6.29 版本
* 兼容 `NERoom` 1.11.0 版本

## v1.0.4(January 12, 2023)
### New Features
* NEVoiceRoomKit新增如下接口：
  - getRoomInfo 查询房间信息
  - setPlayingPosition 指定播放位置
  - pauseEffect 暂停播放音效文件
  - resumeEffect 继续播放音效文件

* NEVoiceRoomListener新增如下接口：
    - onMemberJoinChatroom 成员进入聊天室回调
    - onMemberLeaveChatroom 成员离开聊天室回调
    - onAudioEffectTimestampUpdate 背景音乐播放回调
    - onAudioEffectFinished 本地音效文件播放已结束回调

* 新增NELiveType类，表示直播类型
* NECreateVoiceRoomParams新增liveType，表示直播类型
* NEVoiceRoomCreateAudioEffectOption新增参数startTimestamp表示音效文件的开始播放时间，新增参数progressInterval表示播放进度回调间隔，sendWithAudioType新增参数表示伴音类型

* 兼容 `NIM` 9.6.4 版本
* 兼容 `NERtc` 4.6.29 版本
* 兼容 `NERoom` 1.11.0 版本

## v1.0.3(December 07, 2022)
### New Features
* 新增礼物功能，对齐KTV逻辑
* 断网重连刷新麦位
* 收到ban和unban的时候不再做默认的mute与unmute处理，交由业务侧去控制

* 兼容 `NIM` 9.6.4 版本
* 兼容 `NERtc` 4.6.29 版本
* 兼容 `NERoom` 1.10.0 版本### Compatibility


## v1.0.0(September 30, 2022)
### New Features
* 首次发布版本

### Compatibility
* 兼容 `NERoom` 1.8.2 版本


