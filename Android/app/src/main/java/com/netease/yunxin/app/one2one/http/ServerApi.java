/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.http;

import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.app.one2one.model.UserModel;

import org.json.JSONObject;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServerApi {

    /**
     * 获取Rtc Token
     */
    @POST("/demo/getCheckSum.action")
    Single<BaseResponse> requestRtcToken(@Body Map<String, Object> body);

    /**
     * 根据手机号查询用户信息
     */
    @POST("/p2pVideoCall/caller/searchSubscriber")
    Single<BaseResponse<UserModel>> searchUserWithPhoneNumber(@Body Map<String, Object> body);

    /**
     * 发送普通短信通知
     */
    @POST("/sms/sendOneByOneSms")
    Single<BaseResponse> sendSms(@Body Map<String, Object> body);
}
