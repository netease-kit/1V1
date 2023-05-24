// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERTCVideoCallImpl;

public class NERTCCallStateManager {
  public static void setCallOutState() {
    NERTCVideoCallImpl videoCallImpl = (NERTCVideoCallImpl) NERTCVideoCall.sharedInstance();
    videoCallImpl.getRecorder().setCurrentState(videoCallImpl.getRecorder().getCallOutState());
  }

  public static void setIdleState() {
    NERTCVideoCallImpl videoCallImpl = (NERTCVideoCallImpl) NERTCVideoCall.sharedInstance();
    videoCallImpl.getRecorder().setCurrentState(videoCallImpl.getRecorder().getIdleState());
  }
}
