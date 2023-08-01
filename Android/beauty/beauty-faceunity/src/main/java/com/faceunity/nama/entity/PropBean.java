// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.faceunity.nama.entity;

/** DESC：道具 Created on 2021/4/26 */
public class PropBean {
  private int iconId;
  private String path;

  public PropBean(int iconId, String path) {
    this.iconId = iconId;
    this.path = path;
  }

  public int getIconId() {
    return iconId;
  }

  public void setIconId(int iconId) {
    this.iconId = iconId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
