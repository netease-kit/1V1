// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama;

import com.netease.lava.nertc.sdk.AbsNERtcCallbackEx;
import com.netease.lava.nertc.sdk.NERtcUserJoinExtraInfo;
import com.netease.lava.nertc.sdk.NERtcUserLeaveExtraInfo;
import com.netease.lava.nertc.sdk.video.NERtcVideoStreamType;

public class NERtcCallbackWrapper extends AbsNERtcCallbackEx {

  @Override
  public void onJoinChannel(int result, long channelId, long elapsed, long uid) {}

  @Override
  public void onLeaveChannel(int result) {}

  @Override
  public void onUserJoined(long uid) {}

  @Override
  public void onUserJoined(long uid, NERtcUserJoinExtraInfo joinExtraInfo) {}

  @Override
  public void onUserLeave(long uid, int reason) {}

  @Override
  public void onUserLeave(long uid, int reason, NERtcUserLeaveExtraInfo leaveExtraInfo) {}

  @Override
  public void onUserAudioStart(long uid) {}

  @Override
  public void onUserAudioStop(long uid) {}

  @Override
  public void onUserVideoStart(long uid, int maxProfile) {}

  @Override
  public void onUserVideoStop(long uid) {}

  @Override
  public void onDisconnect(int reason) {}

  @Override
  public void onAudioEffectTimestampUpdate(long id, long timestampMs) {}

  @Override
  public void onError(int code) {}

  @Override
  public void onPermissionKeyWillExpire() {}

  @Override
  public void onUpdatePermissionKey(String key, int error, int timeout) {}

  @Override
  public void onLocalVideoWatermarkState(NERtcVideoStreamType videoStreamType, int state) {}
}
