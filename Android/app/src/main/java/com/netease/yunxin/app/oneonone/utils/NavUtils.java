// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.faceunity.nama.BeautySettingActivity;
import com.netease.yunxin.app.oneonone.activity.CommonSettingActivity;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.ui.activity.OneOnOneHomeActivity;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.entertainment.common.Constants;
import com.netease.yunxin.kit.entertainment.common.R;
import com.netease.yunxin.kit.entertainment.common.activity.WebViewActivity;

public class NavUtils {

  private static final String TAG = "NavUtil";
  private static final String MANUFACTURER_HUAWEI = "HUAWEI"; //华为
  private static final String MANUFACTURER_MEIZU = "Meizu"; //魅族
  private static final String MANUFACTURER_XIAOMI = "Xiaomi"; //小米
  private static final String MANUFACTURER_SONY = "Sony"; //索尼
  private static final String MANUFACTURER_OPPO = "OPPO"; //oppo
  private static final String MANUFACTURER_LG = "LG";
  private static final String MANUFACTURER_VIVO = "vivo"; //vivo
  private static final String MANUFACTURER_SAMSUNG = "samsung"; //三星
  private static final String MANUFACTURER_ZTE = "ZTE"; //中兴
  private static final String MANUFACTURER_YULONG = "YuLong"; //酷派
  private static final String MANUFACTURER_LENOVO = "LENOVO"; //联想

  public static void toPrivacyPolicyPage(Context context) {
    toBrowsePage(
        context, context.getString(R.string.app_privacy_policy), Constants.getPrivacyPolicyUrl());
  }

  public static void toUserPolicePage(Context context) {
    toBrowsePage(
        context, context.getString(R.string.app_user_agreement), Constants.getUserAgreementUrl());
  }

  public static void toBrowsePage(Context context, String title, String url) {
    Intent intent = new Intent(context, WebViewActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    intent.putExtra(Constants.INTENT_KEY_TITLE, title);
    intent.putExtra(Constants.INTENT_KEY_URL, url);
    context.startActivity(intent);
  }

  public static void toBeautySettingPage(Context context) {
    Intent intent = new Intent(context, BeautySettingActivity.class);
    intent.putExtra(BeautySettingActivity.INTENT_KEY_APP_KEY, AppConfig.getAppKey());
    context.startActivity(intent);
  }

  public static void toCommonSettingPage(Context context) {
    Intent intent = new Intent(context, CommonSettingActivity.class);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  public static void toOneOnOneHomePage(Context context) {
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(context.getString(R.string.app_network_error));
      return;
    }
    Intent intent = new Intent(context, OneOnOneHomeActivity.class);
    context.startActivity(intent);
  }

  public static void goToSetting(Activity activity) {
    switch (Build.MANUFACTURER) {
      case MANUFACTURER_HUAWEI:
        Huawei(activity);
        break;
      case MANUFACTURER_MEIZU:
        Meizu(activity);
        break;
      case MANUFACTURER_XIAOMI:
        Xiaomi(activity);
        break;
      case MANUFACTURER_SONY:
        Sony(activity);
        break;
      case MANUFACTURER_OPPO:
        OPPO(activity);
        break;
      case MANUFACTURER_VIVO:
        VIVO(activity);
        break;
      case MANUFACTURER_LG:
        LG(activity);
        break;
      default:
        ApplicationInfo(activity);
        ALog.e(TAG, "目前暂不支持此系统");
        break;
    }
  }

  public static void Huawei(Activity activity) {
    try {
      Intent intent = new Intent();
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra("packageName", activity.getApplicationInfo().packageName);
      ComponentName comp =
          new ComponentName(
              "com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
      intent.setComponent(comp);
      activity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      goIntentSetting(activity);
    }
  }

  public static void Meizu(Activity activity) {
    try {
      Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
      intent.addCategory(Intent.CATEGORY_DEFAULT);
      intent.putExtra("packageName", activity.getPackageName());
      activity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      goIntentSetting(activity);
    }
  }

  public static void Xiaomi(Activity activity) {
    try {
      Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
      intent.putExtra("extra_pkgname", activity.getPackageName());
      ComponentName componentName =
          new ComponentName(
              "com.miui.securitycenter",
              "com.miui.permcenter.permissions.PermissionsEditorActivity");
      intent.setComponent(componentName);
      activity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      goIntentSetting(activity);
    }
  }

  public static void Sony(Activity activity) {
    try {
      Intent intent = new Intent();
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra("packageName", activity.getPackageName());
      ComponentName comp =
          new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
      intent.setComponent(comp);
      activity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      goIntentSetting(activity);
    }
  }

  public static void OPPO(Activity activity) {
    try {
      Intent intent = new Intent();
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra("packageName", activity.getPackageName());
      //        ComponentName comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
      ComponentName comp =
          new ComponentName(
              "com.coloros.securitypermission",
              "com.coloros.securitypermission.permission.PermissionAppAllPermissionActivity"); //R11t 7.1.1 os-v3.2
      intent.setComponent(comp);
      activity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      goIntentSetting(activity);
    }
  }

  public static void VIVO(Activity activity) {
    Intent localIntent;
    if (((Build.MODEL.contains("Y85")) && (!Build.MODEL.contains("Y85A")))
        || (Build.MODEL.contains("vivo Y53L"))) {
      localIntent = new Intent();
      localIntent.setClassName(
          "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity");
      localIntent.putExtra("packagename", activity.getPackageName());
      localIntent.putExtra("tabId", "1");
      activity.startActivity(localIntent);
    } else {
      localIntent = new Intent();
      localIntent.setClassName(
          "com.vivo.permissionmanager",
          "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity");
      localIntent.setAction("secure.intent.action.softPermissionDetail");
      localIntent.putExtra("packagename", activity.getPackageName());
      activity.startActivity(localIntent);
    }
  }

  public static void LG(Activity activity) {
    try {
      Intent intent = new Intent("android.intent.action.MAIN");
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra("packageName", activity.getPackageName());
      ComponentName comp =
          new ComponentName(
              "com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
      intent.setComponent(comp);
      activity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      goIntentSetting(activity);
    }
  }

  /**
   * 只能打开到自带安全软件
   *
   * @param activity
   */
  public static void _360(Activity activity) {
    Intent intent = new Intent("android.intent.action.MAIN");
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra("packageName", activity.getPackageName());
    ComponentName comp =
        new ComponentName(
            "com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
    intent.setComponent(comp);
    activity.startActivity(intent);
  }

  /**
   * 应用信息界面
   *
   * @param activity
   */
  public static void ApplicationInfo(Activity activity) {
    Intent localIntent = new Intent();
    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    if (Build.VERSION.SDK_INT >= 9) {
      localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
      localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
    } else if (Build.VERSION.SDK_INT <= 8) {
      localIntent.setAction(Intent.ACTION_VIEW);
      localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
      localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
    }
    activity.startActivity(localIntent);
  }

  /**
   * 系统设置界面
   *
   * @param activity
   */
  public static void SystemConfig(Activity activity) {
    Intent intent = new Intent(Settings.ACTION_SETTINGS);
    activity.startActivity(intent);
  }

  /** 默认打开应用详细页 */
  private static void goIntentSetting(Activity pActivity) {
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    Uri uri = Uri.fromParts("package", pActivity.getPackageName(), null);
    intent.setData(uri);
    try {
      pActivity.startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
