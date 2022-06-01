/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.utils;

import android.os.Handler;
import android.os.Looper;

public class HandlerUtil {

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void post2MainThread(Runnable runnable){
        mHandler.post(runnable);
    }

    public static void post2MainThreadDelay(Runnable runnable, long delayMillis){
        mHandler.postDelayed(runnable, delayMillis);
    }

}
