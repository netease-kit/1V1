// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.call;

import com.netease.nimlib.sdk.util.Entry;
import com.netease.yunxin.app.one2one.constant.ErrorCode;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.AbsNERtcCallingDelegate;

public class NERtcCallDelegate extends AbsNERtcCallingDelegate {
  private static final String TAG = "NERtcCallDelegateForGroup";

  @Override
  public void onError(int errorCode, String errorMsg, boolean needFinish) {
    LogUtil.e(
        TAG,
        "NERtcCallDelegateForGroup onError->$errorCode, errorMsg:$errorMsg, needFinish:$needFinish");
    if (needFinish) {
      onCallFinished(errorCode, "onError, message is " + errorMsg);
    }
  }

  @Override
  public void onInvited(InvitedInfo invitedInfo) {}

  @Override
  public void onUserEnter(String userId) {}

  @Override
  public void onCallEnd(String userId) {
    onCallFinished(ErrorCode.ERROR, "onCallEnd, user is " + userId);
  }

  @Override
  public void onUserLeave(String userId) {
    onCallFinished(ErrorCode.ERROR, "onUserLeave, user is " + userId);
  }

  @Override
  public void onUserDisconnect(String userId) {
    onCallFinished(ErrorCode.ERROR, "onUserDisconnect, user is " + userId);
  }

  @Override
  public void onRejectByUserId(String userId) {
    onCallFinished(ErrorCode.ERROR, "onRejectByUserId, user is " + userId);
  }

  @Override
  public void onUserBusy(String userId) {
    onCallFinished(ErrorCode.ERROR, "onUserBusy, user is " + userId);
  }

  @Override
  public void onCancelByUserId(String userId) {
    onCallFinished(ErrorCode.ERROR, "onCancelByUserId, user is " + userId);
  }

  @Override
  public void onCameraAvailable(String userId, boolean isVideoAvailable) {}

  @Override
  public void onVideoMuted(String userId, boolean isMuted) {}

  @Override
  public void onAudioMuted(String userId, boolean isMuted) {}

  @Override
  public void onJoinChannel(String accId, long uid, String channelName, long rtcChannelId) {}

  @Override
  public void onAudioAvailable(String userId, boolean isAudioAvailable) {}

  @Override
  public void onDisconnect(int res) {
    onCallFinished(res, "onDisconnect, local user disconnect rtc channel.");
  }

  @Override
  public void onUserNetworkQuality(Entry<String, Integer>[] stats) {}

  @Override
  public void timeOut() {
    onCallFinished(ErrorCode.ERROR, "timeOut");
  }

  @Override
  public void onFirstVideoFrameDecoded(String userId, int width, int height) {}

  protected void onCallFinished(int code, String msg) {}
}
