// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.http;

import com.netease.yunxin.app.oneonone.ui.model.HomeItemModel;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.kit.common.network.Response;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServerApi {

  /** 上报用户心跳 */
  @POST("/nemo/socialChat/user/reporter")
  Call<ModelResponse<Response>> reportHeartBeat();

  /** 获取用户列表 */
  @POST("/nemo/socialChat/user/getOnlineUser")
  Call<ModelResponse<List<HomeItemModel>>> getUserList(@Body Map<String, Object> body);

  /** 获取用户在线状态 */
  @POST("/nemo/socialChat/user/getUserState")
  Call<ModelResponse<String>> getUserState(@Body Map<String, Object> body);

  /** 发送礼物 */
  @POST("/nemo/socialChat/user/reward")
  Call<ModelResponse<Boolean>> reward(@Body Map<String, Object> body);

  /** 获取登录RTCUID */
  @POST("/nemo/socialChat/user/login")
  Call<ModelResponse<User>> loginOneOnOne(@Body Map<String, Object> body);

  /** 根据userUuid获取账号信息 */
  @POST("/nemo/socialChat/user/getUserInfo")
  Call<ModelResponse<User>> getUserInfo(@Body Map<String, Object> body);

  /** 上报Rtc房间创建 */
  @POST("/nemo/track/rtc-room-created")
  Call<ModelResponse<Response>> reportRtcRoom(@Body Map<String, Object> body);
}
