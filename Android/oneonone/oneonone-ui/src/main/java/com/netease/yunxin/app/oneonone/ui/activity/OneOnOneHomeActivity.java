// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avsignalling.SignallingServiceObserver;
import com.netease.nimlib.sdk.avsignalling.event.ChannelCommonEvent;
import com.netease.nimlib.sdk.avsignalling.event.InviteAckEvent;
import com.netease.yunxin.app.oneonone.ui.fragment.HomeFragment;
import com.netease.yunxin.app.oneonone.ui.utils.AssetUtils;
import com.netease.yunxin.app.oneonone.ui.utils.DialogUtil;
import com.netease.yunxin.app.oneonone.ui.utils.ThreadUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.beauty.BeautyManager;
import com.netease.yunxin.kit.beauty.module.NEAssetsEnum;
import com.netease.yunxin.nertc.nertcvideocall.TerminalCode;
import com.netease.yunxin.nertc.nertcvideocall.utils.ExtraInfoUtils;
import java.io.File;
import java.util.Locale;

/** 1v1业务入口页 */
public class OneOnOneHomeActivity extends AppCompatActivity {
  private static final String TAG = "OneOnOneHomeActivity";
  private String extFilesDirPath;

  @RequiresApi(api = Build.VERSION_CODES.FROYO)
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(com.netease.yunxin.app.oneonone.ui.R.layout.activity_oneonone_home);
    getSupportFragmentManager()
        .beginTransaction()
        .add(com.netease.yunxin.app.oneonone.ui.R.id.user_list_content, new HomeFragment())
        .commit();

    BeautyManager.getInstance().init(getApplicationContext());
    extFilesDirPath = getExternalFilesDir(null).getAbsolutePath();
    setupBeautyAssets();
    NIMClient.getService(SignallingServiceObserver.class)
        .observeOnlineNotification(
            new Observer<ChannelCommonEvent>() {
              @Override
              public void onEvent(ChannelCommonEvent channelCommonEvent) {
                if (channelCommonEvent instanceof InviteAckEvent) {
                  String customInfo = channelCommonEvent.getCustomInfo();
                  ALog.d(TAG, "customInfo:" + customInfo);
                  int reason = ExtraInfoUtils.getReason(customInfo);
                  ALog.d(TAG, "reason:" + reason);
                  if (reason == TerminalCode.TERMINAL_CODE_BUSY) {
                    new Handler(Looper.getMainLooper())
                        .postDelayed(
                            () ->
                                DialogUtil.showConfirmDialog(
                                    OneOnOneHomeActivity.this,
                                    getString(
                                        com.netease
                                            .yunxin
                                            .app
                                            .oneonone
                                            .ui
                                            .R
                                            .string
                                            .one_on_one_other_is_busy)),
                            300);
                  }
                }
              }
            },
            true);
  }

  private void setupBeautyAssets() {
    ThreadUtils.execute(
        () -> {
          int ret = -1;
          for (NEAssetsEnum type : NEAssetsEnum.values()) {
            ret =
                AssetUtils.copyAssetRecursive(
                    getAssets(), type.getAssetsPath(), getBeautyAssetPath(type), false);
            if (ret != 0) break;
          }
          if (ret == 0) {
            ALog.i(TAG, "beauty asset is ready");
          } else {
            ALog.i(TAG, "beauty asset is failed");
          }
        });
  }

  /**
   * 生成滤镜和美妆模板资源文件的路径，资源文件在App启动后会拷贝到的App的外部存储路径
   *
   * @param type @see NEAssetsEnum 对应assets目录下的美颜，滤镜或者美妆资源目录
   * @return 美颜，滤镜或者美妆的App外部存储路径
   */
  private String getBeautyAssetPath(NEAssetsEnum type) {
    String separator = File.separator;
    return String.format(
        Locale.getDefault(), "%s%s%s", extFilesDirPath, separator, type.getAssetsPath());
  }
}
