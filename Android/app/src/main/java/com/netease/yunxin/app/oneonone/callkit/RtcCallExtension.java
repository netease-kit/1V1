// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.callkit;

import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcLinkEngine;
import com.netease.yunxin.app.oneonone.ui.constant.AppRtcConfig;
import com.netease.yunxin.app.oneonone.ui.utils.RtcUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.nertcvideocall.model.NERtcCallExtension;
import com.netease.yunxin.nertc.pstn.base.PstnCallbackProxyMgr;

public class RtcCallExtension extends NERtcCallExtension {
  private static final String TAG = "PartyNERtcCallExtension";

  @Override
  public void configVideoConfigBeforeJoin() {
    RtcUtil.setChannelProfile(AppRtcConfig.CHANNEL_PROFILE);
    RtcUtil.configAudioConfig(AppRtcConfig.PROFILE, AppRtcConfig.SCENARIO);
    RtcUtil.configVideoConfig(AppRtcConfig.VIDEO_WIDTH, AppRtcConfig.VIDEO_HEIGHT);
  }

  @Override
  protected void initRtc() {
    try {
      NERtcEx.getInstance().release();
    } catch (Exception e) {
      e.printStackTrace();
      ALog.e(TAG, "e:" + e);
    }
    super.initRtc();
    try {
      NERtcLinkEngine.getInstance().release();
      NERtcLinkEngine.getInstance()
          .init(context, PstnCallbackProxyMgr.INSTANCE.getMainInnerCallback());
    } catch (Exception e) {
      e.printStackTrace();
      ALog.e(TAG, "e:" + e);
    }
  }

  @Override
  protected void releaseRtc() {
    // PSTN需要RTC的初始化，所以需要覆盖呼叫组件的release RTC 方法。呼叫组件不做release
  }
}
