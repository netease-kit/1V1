/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.netease.yunxin.app.one2one.LoginHomeActivity;
import com.netease.yunxin.app.one2one.MainActivity;
import com.netease.yunxin.app.one2one.constant.AppConstants;
import com.netease.yunxin.app.one2one.constant.AppParams;
import com.netease.yunxin.app.one2one.model.UserModel;
import com.netease.yunxin.app.one2one.ui.biz.beauty.BeautySettingActivity;
import com.netease.yunxin.app.one2one.ui.biz.setting.AboutActivity;
import com.netease.yunxin.app.one2one.ui.biz.beauty.BeautySettingActivity;
import com.netease.yunxin.app.one2one.ui.biz.setting.BindOtherPhoneActivity;
import com.netease.yunxin.app.one2one.ui.biz.setting.PSTNSettingActivity;
import com.netease.yunxin.app.one2one.ui.biz.setting.SettingActivity;
import com.netease.yunxin.app.one2one.ui.biz.userinfo.UserInfoActivity;
import com.netease.yunxin.app.one2one.ui.biz.userinfo.UserInfoEditActivity;
import com.netease.yunxin.app.one2one.ui.biz.web.WebViewActivity;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.base.CallParam;

import org.json.JSONException;
import org.json.JSONObject;

public class NavUtils {


    public static void toLoginHomePage(Context context) {
        Intent intent = new Intent(context, LoginHomeActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toLoginPage(Activity context) {
        AuthorManager.INSTANCE.launchLogin(context, AppConstants.MAIN_PAGE_ACTION, true);
    }

    public static void toMainPage(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toSettingPage(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toBindOtherPhonePage(Context context) {
        Intent intent = new Intent(context, BindOtherPhoneActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toPSTNSettingPage(Context context) {
        Intent intent = new Intent(context, PSTNSettingActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toBrowsePage(Context context, String title, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(AppParams.PARAM_KEY_TITLE, title);
        intent.putExtra(AppParams.PARAM_KEY_URL, url);

        context.startActivity(intent);
    }

    public static void toCallPage(Context context, UserModel userModel,int channelType,boolean needPstnCall) {
        JSONObject extraInfo = new JSONObject();
        try {
            extraInfo.putOpt(AppParams.CALLER_USER_NAME, UserInfoManager.getSelfUserInfo().getNickname());
            extraInfo.putOpt(AppParams.CALLER_USER_MOBILE, UserInfoManager.getSelfUserInfo().getUser());
            extraInfo.putOpt(AppParams.CALLER_USER_AVATAR, UserInfoManager.getSelfUserInfo().getAvatar());
            extraInfo.putOpt(AppParams.CALLED_USER_NAME, userModel.nickname);
            extraInfo.putOpt(AppParams.CALLED_USER_MOBILE, userModel.mobile);
            extraInfo.putOpt(AppParams.CALLED_USER_AVATAR, userModel.avatar);
            extraInfo.putOpt(AppParams.NEED_PSTN_CALL, needPstnCall);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CallParam param = CallParam.createSingleCallParam(channelType, UserInfoManager.getSelfImAccid(), userModel.imAccid, extraInfo.toString());
        CallKitUI.startSingleCall(context, param);
    }

    public static void toUserInfoPage(Context context, int position) {
        Intent intent = new Intent(context, UserInfoActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(AppParams.INDEX,position);
        context.startActivity(intent);
    }

    public static void toUserInfoEditPage(Context context) {
//        Intent intent = new Intent(context, UserInfoEditActivity.class);
//        if (!(context instanceof Activity)) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        context.startActivity(intent);
    }

    public static void toBeautySettingPage(Context context) {
        Intent intent = new Intent(context, BeautySettingActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static void toAppAboutPage(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

}
