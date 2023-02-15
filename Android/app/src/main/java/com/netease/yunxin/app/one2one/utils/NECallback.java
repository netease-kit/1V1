// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.utils;

public interface NECallback<T> {
  void onSuccess(T t);

  void onError(int code, String errorMsg);
}
