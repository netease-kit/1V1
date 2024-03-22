// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils.callkit;

import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.yunxin.app.oneonone.ui.constant.AppRtcConfig;
import com.netease.yunxin.app.oneonone.ui.utils.RtcUtil;
import com.netease.yunxin.nertc.nertcvideocall.model.NERtcCallExtension;

public class PartyNERtcCallExtension extends NERtcCallExtension {

  @Override
  public int toJoinChannel(String token, String channelName, long rtcUid) {
    NERtcParameters parameters = new NERtcParameters();
    // 开启服务器录制
    parameters.set(NERtcParameters.KEY_SERVER_RECORD_AUDIO, true);
    parameters.set(NERtcParameters.KEY_SERVER_RECORD_VIDEO, true);
    NERtcEx.getInstance().setParameters(parameters);
    // 该方法仅可在加入房间前调用。
    RtcUtil.setChannelProfile(AppRtcConfig.CHANNEL_PROFILE);
    //该方法在加入房间前后均可调用。
    RtcUtil.configAudioConfig(AppRtcConfig.PROFILE, AppRtcConfig.SCENARIO);
    // 该方法在加入房间前后均可调用。
    RtcUtil.configVideoConfig(AppRtcConfig.VIDEO_WIDTH, AppRtcConfig.VIDEO_HEIGHT);
    configRtcStatsObserver();
    // 该方法在加入房间前后均可调用。
    RtcUtil.enableDualStreamMode(false);
    return super.toJoinChannel(token, channelName, rtcUid);
  }
}
