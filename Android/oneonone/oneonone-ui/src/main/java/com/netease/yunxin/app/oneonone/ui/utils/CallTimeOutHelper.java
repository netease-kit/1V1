// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.netease.yunxin.nertc.pstn.base.PstnFunctionMgr;

public class CallTimeOutHelper {
  public static void configTimeOut(long timeOutMillisecond, long transTimeMillisecond) {
    PstnFunctionMgr.configTimeOut(timeOutMillisecond, transTimeMillisecond);
  }
}
