// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.http;

import android.content.Context;
import com.netease.yunxin.app.oneonone.ui.BuildConfig;
import com.netease.yunxin.app.oneonone.ui.model.HomeItemModel;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.kit.common.network.Response;
import com.netease.yunxin.kit.common.network.ServiceCreator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Callback;

public class HttpService {
  private final ServiceCreator serviceCreator = new ServiceCreator();
  private ServerApi serverApi;

  private static volatile HttpService mInstance;

  private HttpService() {}

  public static HttpService getInstance() {
    if (null == mInstance) {
      synchronized (HttpService.class) {
        if (mInstance == null) {
          mInstance = new HttpService();
        }
      }
    }
    return mInstance;
  }

  public void initialize(Context context, String serverUrl) {
    serviceCreator.init(
        context,
        serverUrl,
        BuildConfig.DEBUG ? ServiceCreator.LOG_LEVEL_BODY : ServiceCreator.LOG_LEVEL_BASIC,
        null);
    serverApi = serviceCreator.create(ServerApi.class);
  }

  public void addHeader(String key, String value) {
    serviceCreator.addHeader(key, value);
  }

  public void reportHeartBeat(Callback<ModelResponse<Response>> callback) {
    if (serverApi != null) {
      serverApi.reportHeartBeat().enqueue(callback);
    }
  }

  public void getUserList(
      int pageNum, int pageSize, Callback<ModelResponse<List<HomeItemModel>>> callback) {
    Map<String, Object> map = new HashMap<>();
    map.put("pageNum", pageNum);
    map.put("pageSize", pageSize);
    if (serverApi != null) {
      serverApi.getUserList(map).enqueue(callback);
    }
  }

  public void getUserState(String mobile, Callback<ModelResponse<String>> callback) {
    Map<String, Object> map = new HashMap<>();
    map.put("mobile", mobile);
    if (serverApi != null) {
      serverApi.getUserState(map).enqueue(callback);
    }
  }

  public void reward(
      int giftId, int giftCount, String target, Callback<ModelResponse<Boolean>> callback) {
    Map<String, Object> map = new HashMap<>();
    map.put("giftId", giftId);
    map.put("giftCount", giftCount);
    map.put("target", target);
    if (serverApi != null) {
      serverApi.reward(map).enqueue(callback);
    }
  }

  public void loginOneOnOne(Callback<ModelResponse<User>> callback) {
    Map<String, Object> map = new HashMap<>();
    if (serverApi != null) {
      serverApi.loginOneOnOne(map).enqueue(callback);
    }
  }

  public void getUserInfo(String userUuid, Callback<ModelResponse<User>> callback) {
    Map<String, Object> map = new HashMap<>();
    map.put("userUuid", userUuid);
    if (serverApi != null) {
      serverApi.getUserInfo(map).enqueue(callback);
    }
  }

  public void reportRtcRoom(long cid, Callback<ModelResponse<Response>> callback) {
    Map<String, Object> map = new HashMap<>();
    map.put("cid", cid + "");
    if (serverApi != null) {
      serverApi.reportRtcRoom(map).enqueue(callback);
    }
  }
}
