// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.beauty.module;

public enum NEAssetsEnum {
  EFFECTS("beauty"),
  FILTERS("filter_portrait"),
  MAKEUPS("makeups");

  private String assetsPath;

  NEAssetsEnum(String assetsPath) {
    this.assetsPath = assetsPath;
  }

  public String getAssetsPath() {
    return assetsPath;
  }
}
