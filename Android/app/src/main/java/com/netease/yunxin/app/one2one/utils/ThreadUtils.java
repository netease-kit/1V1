// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {
  private static ExecutorService executor = Executors.newSingleThreadExecutor();

  public static void execute(Runnable runnable) {
    executor.execute(runnable);
  }
}
