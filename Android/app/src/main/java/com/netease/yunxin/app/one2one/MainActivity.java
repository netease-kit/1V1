/*
 *
 *  * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 *  * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 *
 */

package com.netease.yunxin.app.one2one;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ProcessUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.yunxin.android.lib.network.common.BaseResponse;
import com.netease.yunxin.android.lib.network.common.NetworkClient;
import com.netease.yunxin.app.one2one.base.BaseActivity;
import com.netease.yunxin.app.one2one.constant.AppConstants;
import com.netease.yunxin.app.one2one.constant.AppRtcConfig;
import com.netease.yunxin.app.one2one.constant.CallConfig;
import com.netease.yunxin.app.one2one.databinding.ActivityMainBinding;
import com.netease.yunxin.app.one2one.http.HttpService;
import com.netease.yunxin.app.one2one.ui.biz.beauty.module.NEAssetsEnum;
import com.netease.yunxin.app.one2one.ui.biz.call.CallActivity;
import com.netease.yunxin.app.one2one.ui.biz.home.HomeFragment;
import com.netease.yunxin.app.one2one.ui.biz.home.MessageFragment;
import com.netease.yunxin.app.one2one.ui.biz.home.MineFragment;
import com.netease.yunxin.app.one2one.utils.AssetUtils;
import com.netease.yunxin.app.one2one.utils.LogUtil;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.app.one2one.utils.RtcUtil;
import com.netease.yunxin.app.one2one.utils.ThreadUtils;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.nertc.nertcvideocall.model.TokenService;
import com.netease.yunxin.nertc.pstn.PstnUIHelper;
import com.netease.yunxin.nertc.pstn.base.PstnCallKitOptions;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import com.netease.yunxin.nertc.ui.extension.SelfConfigExtension;

import java.io.File;
import java.util.Locale;

import io.reactivex.observers.ResourceSingleObserver;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private static final int TAB_HOME = 0;
    private static final int TAB_MSG = 1;
    private static final int TAB_MINE = 2;
    private HomeFragment homeFragment;
    private MessageFragment messageFragment;
    private MineFragment mineFragment;
    public static int curTabIndex = -1;
    private String extFilesDirPath;

    @Override
    protected boolean needTransparentStatusBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ImmersionBar bar = ImmersionBar.with(this)
                .statusBarDarkFont(true);
        bar.init();
        NetworkClient.getInstance().configAccessToken(UserInfoManager.getSelfAccessToken());
        selectFragment(TAB_HOME);
        handleEvent();
        if (ProcessUtils.isMainProcess()) {
            initCallKit();
        }
        extFilesDirPath = getExternalFilesDir(null).getAbsolutePath();
        setupBeautyAssets();
    }

    private void handleEvent() {
        binding.tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFragment(TAB_HOME);
            }
        });
        binding.tvMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFragment(TAB_MSG);
            }
        });
        binding.tvMine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFragment(TAB_MINE);
            }
        });
    }

    private void selectFragment(int tabIndex) {
        if (tabIndex == curTabIndex) {
            LogUtil.i(TAG, "tabIndex==curTabIndex");
            return;
        }
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        Fragment currentFragment = supportFragmentManager.findFragmentByTag(curTabIndex + "");
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
            LogUtil.i(TAG, "hide:" + currentFragment);
        }
        curTabIndex = tabIndex;
        Fragment fragment = supportFragmentManager.findFragmentByTag(tabIndex + "");
        if (fragment == null) {
            switch (tabIndex) {
                case TAB_HOME:
                    if (homeFragment == null) {
                        homeFragment = new HomeFragment();
                    }
                    fragment = homeFragment;
                    break;
                case TAB_MSG:
                    if (messageFragment == null) {
                        messageFragment = new MessageFragment();
                    }
                    fragment = messageFragment;
                    break;
                case TAB_MINE:
                    if (mineFragment == null) {
                        mineFragment = new MineFragment();
                    }
                    fragment = mineFragment;
                    break;
                default:
                    break;
            }
            LogUtil.i(TAG, "add:" + fragment);
            fragmentTransaction.add(R.id.fragment_container, fragment, tabIndex + "");
        } else {
            LogUtil.i(TAG, "show:" + fragment);
            fragmentTransaction.show(fragment);
        }
        if (fragment instanceof HomeFragment) {
            handleBottomSelectedState(true, false, false);
        } else if (fragment instanceof MessageFragment) {
            handleBottomSelectedState(false, true, false);
        } else if (fragment instanceof MineFragment) {
            handleBottomSelectedState(false, false, true);
        }
        fragmentTransaction.commit();
    }

    private void handleBottomSelectedState(boolean homeSelected, boolean msgSelected, boolean mineSelected) {
        binding.tvHome.setSelected(homeSelected);
        binding.tvMsg.setSelected(msgSelected);
        binding.tvMine.setSelected(mineSelected);
    }

    private void initCallKit() {
        NERtcOption neRtcOption = new NERtcOption();
        neRtcOption.logLevel = NERtcConstants.LogLevel.INFO;
        CallKitUIOptions options = new CallKitUIOptions.Builder()
                // 必要：音视频通话 sdk appKey，用于通话中使用
                .rtcAppKey(AppConstants.APP_KEY)
                // 必要：当前用户 AccId
                .currentUserAccId(UserInfoManager.getSelfImAccid())
                // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
                .notificationConfigFetcher(invitedInfo -> new CallKitNotificationConfig(R.mipmap.ic_launcher))
                // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
                .resumeBGInvitation(true)
                .rtcCallExtension(new SelfConfigExtension() {
                    @Override
                    public void configVideoConfig() {
                        RtcUtil.configVideoConfig(AppRtcConfig.VIDEO_WIDTH,AppRtcConfig.VIDEO_HEIGHT);
                    }
                })
                .rtcTokenService(new TokenService() {
                    @Override
                    public void getToken(long uid, RequestCallback<String> callback) {
                        HttpService.requestRtcToken(uid).subscribe(new ResourceSingleObserver<BaseResponse>() {
                            @Override
                            public void onSuccess(BaseResponse response) {
                                LogUtil.d("getToken", "response:" + response);
                                if (response.isSuccessful()) {
                                    callback.onSuccess((String) response.data);
                                } else {
                                    callback.onFailed(response.code);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                LogUtil.e("getToken", "e:" + e);
                                callback.onException(e);
                            }
                        });

                    }
                }) // 自己实现的 token 请求方法
                .rtcSdkOption(neRtcOption)
                // 呼叫组件初始化 rtc 范围，true-全局初始化，false-每次通话进行初始化以及销毁
                // 全局初始化有助于更快进入首帧页面，当结合其他组件使用时存在rtc初始化冲突可设置false
                .rtcInitScope(true)
                .p2pAudioActivity(CallActivity.class)
                .p2pVideoActivity(CallActivity.class)
                .build();
// 若重复初始化会销毁之前的初始化实例，重新初始化
        PstnCallKitOptions pstnCallKitOptions = new PstnCallKitOptions.Builder(options)
                .timeOutMillisecond(CallConfig.CALL_TOTAL_WAIT_TIMEOUT)
                .transOutMillisecond(CallConfig.CALL_PSTN_WAIT_MILLISECONDS).build();
        PstnUIHelper.init(getApplicationContext(), pstnCallKitOptions);
    }

    private void setupBeautyAssets() {
        ThreadUtils.execute(() -> {
            int ret = -1;
            for (NEAssetsEnum type : NEAssetsEnum.values()) {
                ret = AssetUtils.copyAssetRecursive(getAssets(), type.getAssetsPath(), getBeautyAssetPath(type), false);
                if (ret != 0) break;
            }
            if (ret == 0) {
                ALog.i(TAG, "beauty asset is ready");
            } else {
                ALog.i(TAG, "beauty asset is failed");
            }
        });
    }

    @Override
    protected void onKickOut() {
        super.onKickOut();
        NavUtils.toLoginHomePage(this);
        finish();
    }

    /**
     * 生成滤镜和美妆模板资源文件的路径，资源文件在App启动后会拷贝到的App的外部存储路径
     *
     * @param type @see NEAssetsEnum 对应assets目录下的美颜，滤镜或者美妆资源目录
     * @return 美颜，滤镜或者美妆的App外部存储路径
     */
    private String getBeautyAssetPath(NEAssetsEnum type) {
        String separator = File.separator;
        return String.format(Locale.getDefault(), "%s%s%s", extFilesDirPath, separator, type.getAssetsPath());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}