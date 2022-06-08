/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one;

import android.app.Application;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ProcessUtils;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.network.common.NetworkClient;
import com.netease.yunxin.app.one2one.constant.AppConstants;
import com.netease.yunxin.app.one2one.http.HttpService;
import com.netease.yunxin.app.one2one.ui.biz.beauty.BeautyManager;
import com.netease.yunxin.app.one2one.ui.biz.call.CallActivity;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.AuthorConfig;
import com.netease.yunxin.kit.login.model.LoginType;
import com.netease.yunxin.nertc.nertcvideocall.model.TokenService;
import com.netease.yunxin.nertc.pstn.PstnUIHelper;
import com.netease.yunxin.nertc.pstn.base.PstnCallKitOptions;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;

import io.reactivex.observers.ResourceSingleObserver;

public class SampleApplication extends Application {
    private final static String TAG = "SampleApp";
    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        wrapperUncaughtExceptionHandler();
        NIMClient.init(this, null, options());
        application = this;
        AuthorConfig authorConfig = new AuthorConfig(AppConstants.APP_KEY, AppConstants.PARENT_SCOPE, AppConstants.SCOPE, false);
        authorConfig.setLoginType(LoginType.LANGUAGE_SWITCH);
        AuthorManager.INSTANCE.initAuthor(getApplicationContext(), authorConfig);
        NetworkClient.getInstance()
                .configBaseUrl(AppConstants.BASE_URL)
                .appKey(AppConstants.APP_KEY)
                .configDebuggable(true);

        String language = getResources().getConfiguration().locale.getLanguage();
        if (!language.contains("zh")) {
            NetworkClient.getInstance().configLanguage("en");
        }
        BeautyManager.getInstance().init(SampleApplication.this);
    }




    public static Application getApplication() {
        return application;
    }

    private void wrapperUncaughtExceptionHandler() {
        ALog.d(TAG, "wrapperUncaughtExceptionHandler");
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (exceptionHandler instanceof InnerExceptionHandler) {
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(new InnerExceptionHandler(exceptionHandler));
    }

    private static class InnerExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler exceptionHandler;

        public InnerExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
        }

        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            ALog.e(TAG, "ThreadName is " + Thread.currentThread().getName() + ", pid is " + android.os.Process.myPid() + " tid is " + android.os.Process.myTid(), e);
            this.exceptionHandler.uncaughtException(t, e);
        }
    }

    // 如果返回值为 null，则全部使用默认参数。
    private SDKOptions options() {
        SDKOptions options = new SDKOptions();
        //此处仅设置appkey，其他设置请自行参看信令文档设置 ：https://dev.yunxin.163.com/docs/product/信令/SDK开发集成/Android开发集成/初始化
        options.appKey = AppConstants.APP_KEY;
        return options;
    }

}
