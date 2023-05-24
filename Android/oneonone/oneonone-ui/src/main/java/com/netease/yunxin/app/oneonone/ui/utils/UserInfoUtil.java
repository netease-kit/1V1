// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.utils;

import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import java.util.HashMap;

public class UserInfoUtil {
  public static UserInfo generateUserInfo(User user) {
    UserInfo userInfo = new UserInfo(user.getUserUuid(), user.getUserName(), user.getIcon());
    userInfo.setMobile(user.getMobile());
    HashMap<String, Object> hashMap = new HashMap<>();
    hashMap.put(AppParams.CALLED_USER_MOBILE, user.getMobile());
    hashMap.put(AppParams.CALL_TYPE, user.getCallType());
    hashMap.put(AppParams.CALLED_AUDIO_URL, user.getAudioUrl());
    hashMap.put(AppParams.CALLED_VIDEO_URL, user.getVideoUrl());
    userInfo.setExtensionMap(hashMap);
    return userInfo;
  }
}
