// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.blankj.utilcode.util.SPUtils;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;

public class AccountAmountHelper {
  private static final String PSTN_CALL_KEY = "pstn_used";
  private static final String SMS_KEY = "sms_used";

  public static boolean allowPstnCall(String account) {
    return getPstnUsedDurationWithAccount(account) < CallConfig.PSTN_CALL_TOTAL_DURATION_SECONDS;
  }

  public static void addPstnUsedDurationWithAccount(String account, long duration) {
    SPUtils.getInstance()
        .put(account + PSTN_CALL_KEY, duration + getPstnUsedDurationWithAccount(account));
  }

  public static long getPstnUsedDurationWithAccount(String account) {
    return SPUtils.getInstance().getLong(account + PSTN_CALL_KEY, 0L);
  }

  public static boolean allowSendSms(String account) {
    return getSmsUsedWithAccount(account) < CallConfig.SMS_TOTAL_COUNT;
  }

  public static void addSmsUsedWithAccount(String account, int count) {
    SPUtils.getInstance().put(account + SMS_KEY, count + getSmsUsedWithAccount(account));
  }

  public static int getSmsUsedWithAccount(String account) {
    return SPUtils.getInstance().getInt(account + SMS_KEY, 0);
  }
}
