// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils.security;

public class SecurityTipsModel {
  public SecurityFoulUser self;
  public SecurityFoulUser other;

  @Override
  public String toString() {
    return "SecurityTipsModel{" + "self=" + self + ", other=" + other + '}';
  }
}
