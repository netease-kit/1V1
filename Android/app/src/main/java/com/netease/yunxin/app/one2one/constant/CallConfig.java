// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.constant;

public class CallConfig {
  /** 呼叫等待时间 */
  public static final long CALL_TOTAL_WAIT_TIMEOUT = 30 * 1000L;
  /** RTC转网络电话呼叫等待时长 */
  public static final long CALL_PSTN_WAIT_MILLISECONDS = 15 * 1000L;
  /** 网络电话总时长 */
  public static final int PSTN_CALL_TOTAL_DURATION_SECONDS = 50 * 60;
  /** 短信总可用条数 */
  public static final int SMS_TOTAL_COUNT = 100;
}
