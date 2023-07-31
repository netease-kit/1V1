// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama;

public class BeautyDataManager {

  private static volatile BeautyDataManager mInstance;

  private BeautyDataManager() {}

  public static BeautyDataManager getInstance() {
    if (null == mInstance) {
      synchronized (BeautyDataManager.class) {
        if (mInstance == null) {
          mInstance = new BeautyDataManager();
        }
      }
    }
    return mInstance;
  }
}
