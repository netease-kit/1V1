// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.http;

import com.netease.yunxin.app.oneonone.ui.model.HomeItemModel;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.UserModel;
import com.netease.yunxin.kit.common.network.Response;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServerApi {

  /** 根据手机号查询用户信息 */
  @POST("/p2pVideoCall/caller/searchSubscriber")
  Call<ModelResponse<UserModel>> searchUserWithPhoneNumber(@Body Map<String, Object> body);

  /** 发送普通短信通知 */
  @POST("/sms/sendOneByOneSms")
  Call<ModelResponse<Response>> sendSms(@Body Map<String, Object> body);

  /** 上报用户心跳 */
  @POST("/user/reporter")
  Call<ModelResponse<Response>> reportHeartBeat();

  /** 获取用户列表 */
  @POST("/user/getUserList")
  Call<ModelResponse<List<HomeItemModel>>> getUserList(@Body Map<String, Object> body);

  /** 获取用户在线状态 */
  @POST("/user/getUserState")
  Call<ModelResponse<String>> getUserState(@Body Map<String, Object> body);
}
