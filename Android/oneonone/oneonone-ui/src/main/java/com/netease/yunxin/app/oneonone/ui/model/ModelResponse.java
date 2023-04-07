// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.model;

import java.io.Serializable;

/** 业务用户数据 */
public final class ModelResponse<T> implements Serializable {
  public int code;
  public T data;
  public String requestId;
  public String costTime;
}
