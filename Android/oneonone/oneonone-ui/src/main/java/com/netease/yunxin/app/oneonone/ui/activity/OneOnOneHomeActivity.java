// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.ui.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.netease.yunxin.app.oneonone.ui.R;
import com.netease.yunxin.app.oneonone.ui.fragment.HomeFragment;
import com.netease.yunxin.kit.call.p2p.NECallEngine;
import com.netease.yunxin.kit.call.p2p.model.NECallEndInfo;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegate;
import com.netease.yunxin.kit.call.p2p.model.NECallEngineDelegateAbs;
import com.netease.yunxin.kit.call.p2p.model.NEHangupReasonCode;
import com.netease.yunxin.kit.entertainment.common.activity.BaseActivity;
import com.netease.yunxin.kit.entertainment.common.utils.DialogUtil;
import java.lang.ref.WeakReference;

/** 1v1业务入口页 */
public class OneOnOneHomeActivity extends BaseActivity {
  private static class MyHandler extends Handler {
    private final WeakReference<OneOnOneHomeActivity> mActivity;

    public MyHandler(OneOnOneHomeActivity activity) {
      mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
      if (msg.what == SHOW_DIALOG_CODE
          && mActivity.get() != null
          && !mActivity.get().isFinishing()) {
        mActivity.get().showBusyDialog();
      }
    }
  }

  private void showBusyDialog() {
    DialogUtil.showAlertDialog(
        OneOnOneHomeActivity.this, getString(R.string.one_on_one_other_is_busy));
  }

  private final MyHandler mHandler = new MyHandler(this);
  private static final int SHOW_DIALOG_CODE = 100;
  private static final String TAG = "OneOnOneHomeActivity";
  private String extFilesDirPath;
  private final NECallEngineDelegate callEngineDelegate =
      new NECallEngineDelegateAbs() {

        @Override
        public void onCallEnd(NECallEndInfo neCallEndInfo) {
          if (neCallEndInfo.reasonCode == NEHangupReasonCode.BUSY
              || (neCallEndInfo.reasonCode == NEHangupReasonCode.CALLER_REJECTED
                  && !TextUtils.isEmpty(neCallEndInfo.extraString))) {
            mHandler.sendEmptyMessageDelayed(SHOW_DIALOG_CODE, 300);
          }
        }
      };

  @RequiresApi(api = Build.VERSION_CODES.FROYO)
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_oneonone_home);
    paddingStatusBarHeight(getRootView(this));
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.user_list_content, new HomeFragment())
        .commit();

    //    BeautyManager.getInstance().init(getApplicationContext());
    extFilesDirPath = getExternalFilesDir(null).getAbsolutePath();
    setupBeautyAssets();
    NECallEngine.sharedInstance().addCallDelegate(callEngineDelegate);
  }

  private void setupBeautyAssets() {
    //    ThreadUtils.execute(
    //        () -> {
    //          int ret = -1;
    //          for (NEAssetsEnum type : NEAssetsEnum.values()) {
    //            ret =
    //                AssetUtils.copyAssetRecursive(
    //                    getAssets(), type.getAssetsPath(), getBeautyAssetPath(type), false);
    //            if (ret != 0) break;
    //          }
    //          if (ret == 0) {
    //            ALog.i(TAG, "beauty asset is ready");
    //          } else {
    //            ALog.i(TAG, "beauty asset is failed");
    //          }
    //        });
  }

  //  /**
  //   * 生成滤镜和美妆模板资源文件的路径，资源文件在App启动后会拷贝到的App的外部存储路径
  //   *
  //   * @param type @see NEAssetsEnum 对应assets目录下的美颜，滤镜或者美妆资源目录
  //   * @return 美颜，滤镜或者美妆的App外部存储路径
  //   */
  //  private String getBeautyAssetPath(NEAssetsEnum type) {
  //    String separator = File.separator;
  //    return String.format(
  //        Locale.getDefault(), "%s%s%s", extFilesDirPath, separator, type.getAssetsPath());
  //  }

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  private static View getRootView(Activity context) {
    return ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    NECallEngine.sharedInstance().removeCallDelegate(callEngineDelegate);
  }
}
