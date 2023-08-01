// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.internal.NECallEngineImpl;

public class NERTCCallStateManager {
  public static void setCallOutState() {
    NECallEngineImpl callEngine = (NECallEngineImpl) NECallEngine.sharedInstance();
    callEngine.getRecorder().setCurrentState(callEngine.getRecorder().getCallOutState());
  }

  public static void setIdleState() {
    NECallEngineImpl callEngine = (NECallEngineImpl) NECallEngine.sharedInstance();
    callEngine.getRecorder().setCurrentState(callEngine.getRecorder().getIdleState());
  }
}
