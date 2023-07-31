// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.adapter.MainPagerAdapter;
import com.netease.yunxin.app.oneonone.callkit.CustomCallOrderHelper;
import com.netease.yunxin.app.oneonone.callkit.RtcCallExtension;
import com.netease.yunxin.app.oneonone.callkit.RtcPushConfigProvider;
import com.netease.yunxin.app.oneonone.config.AppConfig;
import com.netease.yunxin.app.oneonone.databinding.ActivityHomeBinding;
import com.netease.yunxin.app.oneonone.ui.OneOnOneUI;
import com.netease.yunxin.app.oneonone.ui.activity.CallActivity;
import com.netease.yunxin.app.oneonone.ui.constant.AppParams;
import com.netease.yunxin.app.oneonone.ui.constant.CallConfig;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.app.oneonone.ui.model.User;
import com.netease.yunxin.app.oneonone.ui.utils.AppGlobals;
import com.netease.yunxin.app.oneonone.ui.utils.LogUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.call.p2p.model.NECallInitRtcMode;
import com.netease.yunxin.kit.common.network.Response;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.ProcessUtils;
import com.netease.yunxin.kit.entertainment.common.activity.BasePartyActivity;
import com.netease.yunxin.kit.entertainment.common.utils.UserInfoManager;
import com.netease.yunxin.kit.locationkit.LocationKitClient;
import com.netease.yunxin.nertc.nertcvideocall.bean.InvitedInfo;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.ui.CallKitNotificationConfig;
import com.netease.yunxin.nertc.ui.CallKitUI;
import com.netease.yunxin.nertc.ui.CallKitUIOptions;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends BasePartyActivity {
  private static final String TAG = "HomeActivity";
  private ActivityHomeBinding binding;
  public int curTabIndex = -1;
  private Timer timer;
  private TimerTask timerTask;
  private static final int PERIOD = 5 * 1000;

  private Observer<StatusCode> imOnlineStatusObserver =
      new Observer<StatusCode>() {
        public void onEvent(StatusCode status) {
          if (status == StatusCode.LOGINED) {

            if (ProcessUtils.isMainProcess(HomeActivity.this)
                && !TextUtils.equals(
                    CallKitUI.INSTANCE.getCurrentUserAccId(), UserInfoManager.getSelfImAccid())) {
              NERtcParameters parameters = new NERtcParameters();
              // 开启服务器录制
              parameters.set(NERtcParameters.KEY_SERVER_RECORD_AUDIO, true);
              parameters.set(NERtcParameters.KEY_SERVER_RECORD_VIDEO, true);
              NERtcEx.getInstance().setParameters(parameters);

              NERtcOption neRtcOption = new NERtcOption();
              neRtcOption.logLevel = NERtcConstants.LogLevel.INFO;
              HttpService.getInstance()
                  .loginOneOnOne(
                      new Callback<ModelResponse<User>>() {
                        @Override
                        public void onResponse(
                            Call<ModelResponse<User>> call,
                            retrofit2.Response<ModelResponse<User>> response) {
                          if (response == null
                              || response.body() == null
                              || response.body().data == null) {
                            ALog.e(TAG, "loginOneOnOne failed");
                            return;
                          }
                          long customRtcUid = response.body().data.getRtcUid();
                          CallKitUIOptions options =
                              new CallKitUIOptions.Builder()
                                  // 必要：音视频通话 sdk appKey，用于通话中使用
                                  .rtcAppKey(AppConfig.getAppKey())
                                  // 非必要：这里是设置自定义Rtc uid，这里主要用于本Demo的业务服务器处理安全通业务逻辑，在音视频违规时，
                                  // 本Demo的业务服务器会收到带有RTC uid的违规信息，会基于Rtc uid反查IM accId，通过IM自定义消息的方式（PassthroughServiceObserve）告知客户端音视频违规
                                  .currentUserRtcUId(customRtcUid)
                                  // 必要：当前用户 AccId
                                  .currentUserAccId(UserInfoManager.getSelfImAccid())
                                  .timeOutMillisecond(CallConfig.CALL_TOTAL_WAIT_TIMEOUT)
                                  .enableAutoJoinWhenCalled(true)
                                  // 此处为 收到来电时展示的 notification 相关配置，如图标，提示语等。
                                  .notificationConfigFetcher(
                                      invitedInfo -> {
                                        ALog.i(TAG, "invitedInfo:" + invitedInfo.toString());
                                        return generateNotificationConfig(invitedInfo);
                                      })
                                  .pushConfigProvider(new RtcPushConfigProvider())
                                  // 收到被叫时若 app 在后台，在恢复到前台时是否自动唤起被叫页面，默认为 true
                                  .resumeBGInvitation(true)
                                  .joinRtcWhenCall(true)
                                  .rtcCallExtension(new RtcCallExtension())
                                  .rtcSdkOption(neRtcOption)
                                  .initRtcMode(NECallInitRtcMode.IN_NEED_DELAY_TO_ACCEPT)
                                  .p2pAudioActivity(CallActivity.class)
                                  .p2pVideoActivity(CallActivity.class)
                                  .build();
                          NERTCVideoCall.sharedInstance()
                              .setCallOrderListener(new CustomCallOrderHelper());
                          CallKitUI.init(AppGlobals.getApplication(), options);
                        }

                        @Override
                        public void onFailure(Call<ModelResponse<User>> call, Throwable t) {
                          ALog.e(TAG, "loginOneOnOne failed,t:" + t);
                        }
                      });
            }
          } else if (status == StatusCode.KICKOUT || status == StatusCode.KICK_BY_OTHER_CLIENT) {
            stopHeartBeatReportTask();
          }
        }
      };

  private CallKitNotificationConfig generateNotificationConfig(InvitedInfo invitedInfo) {
    CallKitNotificationConfig callKitNotificationConfig;
    String nickname = invitedInfo.currentAccId;
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(invitedInfo.attachment);
      nickname = jsonObject.optString(AppParams.CALLER_USER_NAME);
    } catch (JSONException e) {
      e.printStackTrace();
      ALog.e(TAG, "e:" + e);
    }
    if (invitedInfo.channelType == ChannelType.AUDIO.getValue()) {
      callKitNotificationConfig =
          new CallKitNotificationConfig(
              R.mipmap.ic_launcher,
              null,
              getString(R.string.one_on_one_app_name),
              nickname + getString(R.string.app_notification_new_incoming_audio_call));
    } else {
      callKitNotificationConfig =
          new CallKitNotificationConfig(
              R.mipmap.ic_launcher,
              null,
              getString(R.string.one_on_one_app_name),
              nickname + getString(R.string.app_notification_new_incoming_video_call));
    }

    return callKitNotificationConfig;
  }

  @Override
  protected View getRootView() {
    binding = ActivityHomeBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void init() {
    curTabIndex = -1;
    LocationKitClient.init(this);
    login(AppConfig.IM_ACCID, AppConfig.IM_TOKEN);
    initViews();
  }

  private void initOneOnOne() {
    OneOnOneUI.getInstance()
        .initialize(this, AppConfig.getOneOnOneBaseUrl(), AppConfig.getAppKey());
    OneOnOneUI.getInstance().setChineseEnv(AppConfig.isChineseEnv());
    OneOnOneUI.getInstance()
        .addHttpHeader(UserInfoManager.getSelfUserToken(), UserInfoManager.getSelfImAccid());
    NIMClient.getService(AuthServiceObserver.class)
        .observeOnlineStatus(imOnlineStatusObserver, true);
  }

  private void startHeartBeatReportTask() {
    timer = new Timer();
    if (timerTask != null) {
      timerTask.cancel();
    }
    timerTask =
        new TimerTask() {
          @Override
          public void run() {
            HttpService.getInstance()
                .reportHeartBeat(
                    new Callback<ModelResponse<Response>>() {
                      @Override
                      public void onResponse(
                          Call<ModelResponse<Response>> call,
                          retrofit2.Response<ModelResponse<Response>> response) {}

                      @Override
                      public void onFailure(Call<ModelResponse<Response>> call, Throwable t) {}
                    });
          }
        };
    timer.schedule(timerTask, 0, PERIOD);
  }

  private void initViews() {
    MainPagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
    binding.vpFragment.setAdapter(pagerAdapter);
    binding.vpFragment.setOffscreenPageLimit(2);
    binding.tlTab.setupWithViewPager(binding.vpFragment);
    binding.tlTab.removeAllTabs();
    binding.tlTab.setTabGravity(TabLayout.GRAVITY_CENTER);
    binding.tlTab.setSelectedTabIndicator(null);
    binding.tlTab.addTab(
        binding.tlTab.newTab().setCustomView(R.layout.view_item_home_tab_app), 0, true);
    binding.tlTab.addTab(
        binding.tlTab.newTab().setCustomView(R.layout.view_item_home_tab_message), 1, false);
    binding.tlTab.addTab(
        binding.tlTab.newTab().setCustomView(R.layout.view_item_home_tab_user), 2, false);
    binding.vpFragment.addOnPageChangeListener(
        new TabLayout.TabLayoutOnPageChangeListener(binding.tlTab) {

          @Override
          public void onPageSelected(int position) {
            TabLayout.Tab item = binding.tlTab.getTabAt(position);
            if (item != null) {
              item.select();
            }
            super.onPageSelected(position);
          }
        });
    pagerAdapter.setUnreadCountCallback(
        new MainPagerAdapter.UnreadCountCallback() {

          @Override
          public void onUnreadCountChange(int unreadCount) {
            TextView unreadTV = binding.tlTab.findViewById(R.id.tv_unread);
            String content;
            if (unreadCount >= 100) {
              content = "99+";
            } else {
              content = String.valueOf(unreadCount);
            }
            unreadTV.setText(content);
            unreadTV.setVisibility(unreadCount > 0 ? View.VISIBLE : View.GONE);
          }
        });
  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    curTabIndex = -1;
    if (imOnlineStatusObserver != null) {
      NIMClient.getService(AuthServiceObserver.class)
          .observeOnlineStatus(imOnlineStatusObserver, false);
    }
    ALog.flush(true);
  }

  private void stopHeartBeatReportTask() {
    if (timer != null) {
      timer.cancel();
    }
  }

  private void login(String imAccid, String imToken) {
    if (TextUtils.isEmpty(imAccid)) {
      ALog.d(TAG, "login but imAccid is empty");
      return;
    }
    if (TextUtils.isEmpty(imToken)) {
      ALog.d(TAG, "login but token is empty");
      return;
    }
    UserInfoManager.setIMUserInfo(
        AppConfig.IM_ACCID,
        AppConfig.IM_TOKEN,
        AppConfig.IM_NICKNAME,
        AppConfig.IM_AVATAR,
        "");
    UserInfoManager.setSelfUserToken(AppConfig.USER_TOKEN);
    //登录云信IM
    LoginInfo info = new LoginInfo(imAccid, imToken);
    RequestCallback<LoginInfo> callback =
        new RequestCallback<LoginInfo>() {
          @Override
          public void onSuccess(LoginInfo param) {
            LogUtil.i(TAG, "login success");
            ToastX.showShortToast("云信IM登录成功");
            initOneOnOne();
            startHeartBeatReportTask();
            // your code
          }

          @Override
          public void onFailed(int code) {
            ToastX.showShortToast("云信IM登录失败,code:" + code);
            if (code == 302) {
              LogUtil.i(TAG, "账号密码错误");
              // your code
            } else {
              // your code
            }
          }

          @Override
          public void onException(Throwable exception) {
            // your code
          }
        };

    //执行手动登录
    NIMClient.getService(AuthService.class).login(info).setCallback(callback);
  }
}
