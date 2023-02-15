// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.http;

import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.network.common.NetworkClient;
import com.netease.yunxin.app.one2one.model.UserModel;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.HashMap;
import java.util.Map;

public class HttpService {
  public static Single<BaseResponse<UserModel>> searchUserInfoWithPhoneNumber(String phoneNumber) {
    ServerApi serverApi = NetworkClient.getInstance().getService(ServerApi.class);
    Map<String, Object> map = new HashMap<>();
    map.put("mobile", phoneNumber);
    return serverApi
        .searchUserWithPhoneNumber(map)
        .compose(scheduleThread())
        .map(userModelBaseResponse -> userModelBaseResponse);
  }

  public static Single<BaseResponse> sendSms(String phoneNumber, String noticeUser) {
    ServerApi serverApi = NetworkClient.getInstance().getService(ServerApi.class);
    Map<String, Object> map = new HashMap<>();
    map.put("mobile", phoneNumber);
    map.put("noticeUser", noticeUser);
    return serverApi.sendSms(map).compose(scheduleThread()).map(baseResponse -> baseResponse);
  }

  /** * 切换网络访问线程 */
  private static <T> SingleTransformer<T, T> scheduleThread() {
    return upstream ->
        upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }
}
