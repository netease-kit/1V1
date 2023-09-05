// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.oneonone.activity;

import android.view.View;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.yunxin.app.oneonone.R;
import com.netease.yunxin.app.oneonone.adapter.MainPagerAdapter;
import com.netease.yunxin.app.oneonone.databinding.ActivityHomeBinding;
import com.netease.yunxin.app.oneonone.ui.http.HttpService;
import com.netease.yunxin.app.oneonone.ui.model.ModelResponse;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.network.Response;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.entertainment.common.activity.BasePartyActivity;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends BasePartyActivity {
  private static final String TAG = "HomeActivity";
  private ActivityHomeBinding binding;
  public int curTabIndex = -1;
  private Timer timer;
  private TimerTask timerTask;
  private static final int PERIOD = 5 * 1000;

  private final Observer<StatusCode> imOnlineStatusObserver =
      new Observer<StatusCode>() {
        public void onEvent(StatusCode status) {
          if (status == StatusCode.LOGINED) {
            ALog.i(TAG, "im status:login success");
          } else if (status == StatusCode.KICKOUT) {
            ToastX.showShortToast("im status:kickout");
            stopHeartBeatReportTask();
          } else if (status == StatusCode.KICK_BY_OTHER_CLIENT) {
            ToastX.showShortToast("im status:kickout by other client");
            stopHeartBeatReportTask();
          } else if (status == StatusCode.NET_BROKEN) {
            ToastX.showShortToast("im status:network broken");
          } else if (status == StatusCode.PWD_ERROR) {
            ToastX.showShortToast("im status:pwd error");
          } else if (status == StatusCode.FORBIDDEN) {
            ToastX.showShortToast("im status:forbidden");
          } else if (status == StatusCode.LOGINING) {
            ToastX.showShortToast("im logining");
          } else {
            ToastX.showShortToast("im login failed");
          }
          //判断当前状态是否要进行手动登录。
          if (SampleLoginActivity.shouldJumpToLoginActivity()) {
            SampleLoginActivity.startLoginActivity(HomeActivity.this);
          }
        }
      };

  @Override
  protected View getRootView() {
    binding = ActivityHomeBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void init() {
    curTabIndex = -1;
    initViews();
    NIMClient.getService(AuthServiceObserver.class)
        .observeOnlineStatus(imOnlineStatusObserver, true);
    startHeartBeatReportTask();
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
}
