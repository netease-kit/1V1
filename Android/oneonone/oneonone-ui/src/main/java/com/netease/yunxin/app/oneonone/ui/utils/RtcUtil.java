// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import android.content.Context;
import android.media.AudioManager;
import com.netease.lava.nertc.sdk.*;
import com.netease.lava.nertc.sdk.video.NERtcVideoConfig;
import com.netease.yunxin.app.oneonone.ui.constant.AppRtcConfig;

public class RtcUtil {
  public static void configVideoConfig(int videoWidth, int videoHeight) {
    NERtcVideoConfig videoConfig = new NERtcVideoConfig();
    videoConfig.frameRate = AppRtcConfig.VIDEO_FRAME_RATE;
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

  public static void enableDualStreamMode(boolean enable) {
    NERtcEx.getInstance().enableDualStreamMode(enable);
  }

  public static void setSpeakerphoneOn(Context context, boolean speakerphoneOn) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    if (speakerphoneOn) {
      audioManager.setMode(AudioManager.MODE_NORMAL);
      audioManager.setSpeakerphoneOn(true);
    } else {
      audioManager.setMode(AudioManager.MODE_IN_CALL);
      audioManager.setSpeakerphoneOn(false);
    }
  }

  public static void resetAudioManagerMode(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    audioManager.setMode(AudioManager.MODE_NORMAL);
    audioManager.setSpeakerphoneOn(false);
  }
}
