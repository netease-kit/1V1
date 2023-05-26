// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.model;

public class HomeItemModel {
  public String icon;
  public String userName;
  public String userUuid;
  public String mobile;
  public String audioUrl;
  public String videoUrl;
  public int callType;
  public int age;
  public long firstReportTime;
  public long lastReportTime;

  public HomeItemModel(String imageUrl, String userName, int age) {
    this.icon = imageUrl;
    this.userName = userName;
    this.age = age;
  }

  public HomeItemModel() {}
}
