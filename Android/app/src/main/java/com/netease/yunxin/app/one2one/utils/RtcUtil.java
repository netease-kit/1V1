/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.utils;

import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.video.NERtcVideoConfig;
import com.netease.yunxin.app.one2one.constant.AppRtcConfig;

public class RtcUtil {
    public static void configVideoConfig(int videoWidth,int videoHeight){
        NERtcVideoConfig videoConfig = new NERtcVideoConfig();
        videoConfig.frameRate = AppRtcConfig.VIDEO_FRAME_RATE;
        videoConfig.width = videoWidth;
        videoConfig.height = videoHeight;
        NERtcEx.getInstance().setLocalVideoConfig(videoConfig);
    }
}
