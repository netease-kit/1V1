/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.constant;

import com.netease.lava.nertc.sdk.video.NERtcEncodeConfig;

public class AppRtcConfig {
    /**
     * 预览分辨率-宽
     */
    public static final int VIDEO_PREVIEW_WIDTH = 1920;
    /**
     * 预览分辨率-高
     */
    public static final int VIDEO_PREVIEW_HEIGHT = 1080;
    /**
     * 通话中分辨率-宽
     */
    public static final int VIDEO_WIDTH = 1280;
    /**
     * 通话中分辨率-高
     */
    public static final int VIDEO_HEIGHT = 720;
    public static final NERtcEncodeConfig.NERtcVideoFrameRate VIDEO_FRAME_RATE = NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_24;
}
