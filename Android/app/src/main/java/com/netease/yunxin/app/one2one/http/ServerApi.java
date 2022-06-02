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
     * 获取Rtc Token  https://nrtc.netease.im/demo/getChecksum.action?uid=30569704039&appkey=56813bdfbaa1c2a29bbea391ffbbe27a
     * {
     * 	"code": 200,
     * 	"checksum": "4ac9066de56ba7259dddb45a4334b8838a987dcb"
     * }
     *
     *
     * {
     * 	"code": 200,
     * 	"data": "c4a69f571a1291ebe7729a906668b62f138b1b9d",
     * 	"requestId": "abd1e08202204141725273050020000",
     * 	"costTime": "96ms"
     * }
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
