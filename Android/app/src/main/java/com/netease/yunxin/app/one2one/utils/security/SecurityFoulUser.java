// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils.security;

public class SecurityFoulUser {
  /** 音视频房间的用户Id */
  public long rtcUid;
  /**
   * 违规类型
   *
   * @see SecurityType
   */
  public int type;

  /** 违规文案 */
  public String tips;
}
