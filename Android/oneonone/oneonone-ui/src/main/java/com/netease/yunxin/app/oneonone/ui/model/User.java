// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.model;

import java.io.Serializable;

public class User implements Serializable {
  private String userUuid;
  private String userName;
  private String icon;
  private String mobile;
  private long rtcUid;
  private String userToken;
  private String imToken;
  private int sex;
  private String audioUrl;
  private String videoUrl;
  private int callType;

  public String getAudioUrl() {
    return audioUrl;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public int getCallType() {
    return callType;
  }

  public String getUserUuid() {
    return userUuid;
  }

  public void setUserUuid(String userUuid) {
    this.userUuid = userUuid;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public long getRtcUid() {
    return rtcUid;
  }

  public void setRtcUid(long rtcUid) {
    this.rtcUid = rtcUid;
  }

  public String getUserToken() {
    return userToken;
  }

  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }

  public String getImToken() {
    return imToken;
  }

  public void setImToken(String imToken) {
    this.imToken = imToken;
  }

  public int getSex() {
    return sex;
  }

  public void setSex(int sex) {
    this.sex = sex;
  }
}
