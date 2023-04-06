// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.beauty;

import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.video.NERtcVideoConfig;

public class RtcUtil {
  public static void configVideoConfig(int videoWidth, int videoHeight) {
    NERtcVideoConfig videoConfig = new NERtcVideoConfig();
    videoConfig.frameRate = BeautyConfig.VIDEO_FRAME_RATE;
    videoConfig.width = videoWidth;
    videoConfig.height = videoHeight;
    //    videoConfig.bitrate = AppRtcConfig.BITRATE;
    NERtcEx.getInstance().setLocalVideoConfig(videoConfig);
  }

  public static void configAudioConfig(int profile, int scenario) {
    NERtcEx.getInstance().setAudioProfile(profile, scenario);
  }

  public static void setChannelProfile(int channelProfile) {
    NERtcEx.getInstance().setChannelProfile(channelProfile);
  }
}
