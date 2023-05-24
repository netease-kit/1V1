// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.constant;

import com.netease.lava.nertc.sdk.*;
import com.netease.lava.nertc.sdk.video.NERtcEncodeConfig;

public class AppRtcConfig {
  /** 预览分辨率-宽 */
  public static final int VIDEO_PREVIEW_WIDTH = 360;
  /** 预览分辨率-高 */
  public static final int VIDEO_PREVIEW_HEIGHT = 640;
  /** 通话中分辨率-宽 */
  public static final int VIDEO_WIDTH = 360;
  /** 通话中分辨率-高 */
  public static final int VIDEO_HEIGHT = 640;
  /** 码率 */
  public static final int BITRATE = 1000;

  public static final int PROFILE = NERtcConstants.AudioProfile.HIGH_QUALITY;
  public static final int SCENARIO = NERtcConstants.AudioScenario.SPEECH;
  public static final int CHANNEL_PROFILE = NERtcConstants.RTCChannelProfile.LIVE_BROADCASTING;

  public static final NERtcEncodeConfig.NERtcVideoFrameRate VIDEO_FRAME_RATE =
      NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_24;
}
