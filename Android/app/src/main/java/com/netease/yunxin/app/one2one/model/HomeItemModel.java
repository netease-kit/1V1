// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.model;

public class HomeItemModel {
  public String imageUrl;
  public String nickName;
  public int age;

  public HomeItemModel(String imageUrl, String nickName, int age) {
    this.imageUrl = imageUrl;
    this.nickName = nickName;
    this.age = age;
  }

  public HomeItemModel() {}
}
