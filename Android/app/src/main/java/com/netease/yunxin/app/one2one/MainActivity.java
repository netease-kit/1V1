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
                // ???????????????????????? sdk appKey????????????????????????
                .rtcAppKey(AppConstants.APP_KEY)
                // ????????????????????? AccId
                .currentUserAccId(UserInfoManager.getSelfImAccid())
                // ????????? ???????????????????????? notification ??????????????????????????????????????????
                .notificationConfigFetcher(invitedInfo -> new CallKitNotificationConfig(R.mipmap.ic_launcher))
                // ?????????????????? app ??????????????????????????????????????????????????????????????????????????? true
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
                }) // ??????????????? token ????????????
                .rtcSdkOption(neRtcOption)
                // ????????????????????? rtc ?????????true-??????????????????false-???????????????????????????????????????
                // ???????????????????????????????????????????????????????????????????????????????????????rtc????????????????????????false
                .rtcInitScope(true)
                .p2pAudioActivity(CallActivity.class)
                .p2pVideoActivity(CallActivity.class)
                .build();
// ?????????????????????????????????????????????????????????????????????
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
     * ??????????????????????????????????????????????????????????????????App????????????????????????App?????????????????????
     *
     * @param type @see NEAssetsEnum ??????assets???????????????????????????????????????????????????
     * @return ??????????????????????????????App??????????????????
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