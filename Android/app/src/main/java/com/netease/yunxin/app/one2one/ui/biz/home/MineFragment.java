// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.one2one.ui.biz.home;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.blankj.utilcode.util.ToastUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.lava.api.model.RTCLastmileQuality;
import com.netease.lava.api.model.stats.RTCLastmileProbeResultStatus;
import com.netease.lava.nertc.sdk.LastmileProbeConfig;
import com.netease.lava.nertc.sdk.LastmileProbeResult;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.yunxin.android.lib.picture.ImageLoader;
import com.netease.yunxin.app.one2one.R;
import com.netease.yunxin.app.one2one.constant.AppConfig;
import com.netease.yunxin.app.one2one.databinding.FragmentMineBinding;
import com.netease.yunxin.app.one2one.ui.dialog.DataCenterDialog;
import com.netease.yunxin.app.one2one.ui.dialog.NetworkInfoDialog;
import com.netease.yunxin.app.one2one.ui.view.MineItemView;
import com.netease.yunxin.app.one2one.utils.AppGlobals;
import com.netease.yunxin.app.one2one.utils.AppStates;
import com.netease.yunxin.app.one2one.utils.NavUtils;
import com.netease.yunxin.app.one2one.utils.UserInfoManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.LoadingDialog;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.LoginCallback;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackExTemp;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.NERtcCallbackProxyMgr;
import java.util.List;

/** mine tab fragment */
public class MineFragment extends Fragment {
  private static final String TAG = "MineFragment";
  private FragmentMineBinding binding;
  private Activity activity;
  private int count = 0;
  private int quality = -1;
  private static final int CALLBACK_TOTAL_COUNT = 2;
  private LastmileProbeResult probeResult;
  private View.OnClickListener dataCenterChangeListener;
  private NERtcCallbackExTemp neRtcCallbackExTemp =
      new NERtcCallbackExTemp() {
        @Override
        public void onLastmileQuality(int quality) {
          super.onLastmileQuality(quality);
          ALog.d(TAG, "onLastmileQuality,quality:" + quality);
          count++;
          mergeInfo(quality, MineFragment.this.probeResult);
        }

        @Override
        public void onLastmileProbeResult(LastmileProbeResult result) {
          super.onLastmileProbeResult(result);
          ALog.d(TAG, "onLastmileProbeResult,result:" + result);
          count++;
          mergeInfo(MineFragment.this.quality, result);
        }
      };

  private void mergeInfo(int quality, LastmileProbeResult probeResult) {
    this.quality = quality;
    this.probeResult = probeResult;
    if (count == CALLBACK_TOTAL_COUNT) {
      toggleLoading(false);
      NERtcEx.getInstance().stopLastmileProbeTest();
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder
          .append(getString(R.string.network_quality))
          .append(covertQuality(this.quality))
          .append("\n")
          .append(getString(R.string.quality_result))
          .append(covertState(this.probeResult.state))
          .append("\n")
          .append(getString(R.string.network_rtt))
          .append(this.probeResult.rtt + "ms")
          .append("\n")
          .append(getString(R.string.network_up_packet_loss_rate))
          .append(this.probeResult.uplinkReport.packetLossRate + "%")
          .append("\n")
          .append(getString(R.string.network_up_jitter))
          .append(this.probeResult.uplinkReport.jitter + "ms")
          .append("\n")
          .append(getString(R.string.network_up_avaliable_band_width))
          .append(this.probeResult.uplinkReport.availableBandwidth + "bps")
          .append("\n")
          .append(getString(R.string.network_down_packet_loss_rate))
          .append(this.probeResult.downlinkReport.packetLossRate + "%")
          .append("\n")
          .append(getString(R.string.network_down_jitter))
          .append(this.probeResult.downlinkReport.jitter + "ms")
          .append("\n")
          .append(getString(R.string.network_down_available_band_width))
          .append(this.probeResult.downlinkReport.availableBandwidth + "bps");
      NetworkInfoDialog dialog = new NetworkInfoDialog(requireActivity());
      dialog.setContent(stringBuilder.toString());
      dialog.setDialogCallback(Dialog::dismiss);
      dialog.show();
      count = 0;
      this.quality = -1;
      this.probeResult = null;
    }
  }

  private String covertQuality(int quality) {
    if (quality == RTCLastmileQuality.QUALITY_UNKNOWN) {
      return getString(R.string.quality_unknown);
    } else if (quality == RTCLastmileQuality.QUALITY_EXCELLENT) {
      return getString(R.string.quality_excellent);
    } else if (quality == RTCLastmileQuality.QUALITY_GOOD) {
      return getString(R.string.quality_good);
    } else if (quality == RTCLastmileQuality.QUALITY_POOR) {
      return getString(R.string.quality_poor);
    } else if (quality == RTCLastmileQuality.QUALITY_BAD) {
      return getString(R.string.quality_bad);
    } else if (quality == RTCLastmileQuality.QUALITY_VBAD) {
      return getString(R.string.quality_vbad);
    } else if (quality == RTCLastmileQuality.QUALITY_DOWN) {
      return getString(R.string.quality_down);
    }
    return "";
  }

  private String covertState(short state) {
    if (state == RTCLastmileProbeResultStatus.LASTMILE_PROBE_RESULT_COMPLETE) {
      return getString(R.string.state_result_complete);
    } else if (state == RTCLastmileProbeResultStatus.LASTMILE_PROBE_RESULT_INCOMPLETE_NO_BWE) {
      return getString(R.string.state_result_incomplete_no_bwe);
    } else if (state == RTCLastmileProbeResultStatus.LASTMILE_PROBE_RESULT_UNAVAILABLE) {
      return getString(R.string.state_result_unavailable);
    }
    return "";
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    activity = (Activity) context;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMineBinding.inflate(inflater, container, false);
    initView();
    initEvent();
    initDataCenter();
    listenNetworkProbInfo();
    return binding.getRoot();
  }

  private void listenNetworkProbInfo() {
    NERtcCallbackProxyMgr.getInstance().addCallback(neRtcCallbackExTemp);
  }

  private void initEvent() {
    binding.tvNickname.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            NavUtils.toUserInfoEditPage(activity);
          }
        });

    binding.ivAvatar.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            NavUtils.toUserInfoEditPage(activity);
          }
        });

    binding.itemViewDiamond.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            ToastUtils.showShort(R.string.todo_fuction_tips);
          }
        });
    binding.itemViewFollow.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            ToastUtils.showShort(R.string.todo_fuction_tips);
          }
        });
    binding.itemViewLove.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            ToastUtils.showShort(R.string.todo_fuction_tips);
          }
        });
    binding.itemViewBeauty.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toBeautySettingPage(activity);
          }
        });
    binding.itemViewSetting.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NavUtils.toSettingPage(activity);
          }
        });
    binding.itemViewLogUpload.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NERtcEx.getInstance().uploadSdkInfo();
            ToastUtils.showLong(R.string.please_wait_five_second_upload);
          }
        });
    binding.itemViewNetwork.setOnItemClickListener(
        new MineItemView.OnItemClickListener() {
          @Override
          public void onItemClick() {
            NERtcEx.getInstance().startLastmileProbeTest(new LastmileProbeConfig());
            toggleLoading(true);
          }
        });
  }

  private void initView() {
    ImmersionBar.with(this).statusBarView(binding.statusBarHolder).statusBarDarkFont(true).init();
    if (UserInfoManager.getSelfUserInfo() != null) {
      binding.tvNickname.setText(UserInfoManager.getSelfUserInfo().getNickname());
      ImageLoader.with(AppGlobals.getApplication())
          .circleLoad(UserInfoManager.getSelfUserInfo().getAvatar(), binding.ivAvatar);
    }
  }

  private void initDataCenter() {
    if (AppConfig.getDataCenter() == AppConfig.DataCenter.MainLand) {
      binding.dataCenterMainland.setChecked(true);
    } else {
      binding.dataCenterOversea.setChecked(true);
    }
    if (dataCenterChangeListener == null) {
      dataCenterChangeListener =
          (buttonView) -> {
            final AppConfig.DataCenter dataCenter =
                buttonView == binding.dataCenterMainland
                    ? AppConfig.DataCenter.MainLand
                    : AppConfig.DataCenter.Oversea;
            if (dataCenter != AppConfig.getDataCenter()) {
              DataCenterDialog tipsDialog = new DataCenterDialog(requireActivity());
              tipsDialog.setDialogCallback(
                  new DataCenterDialog.TipsDialogCallback() {
                    @Override
                    public void onConfirm(Dialog dialog) {
                      AppConfig.setDataCenter(dataCenter);
                      logoutThenQuitApp(() -> initDataCenter());
                      dialog.dismiss();
                    }

                    @Override
                    public void onCancel(Dialog dialog) {
                      initDataCenter();
                      dialog.dismiss();
                    }
                  });
              tipsDialog.show();
            }
          };
      binding.dataCenterMainland.setOnClickListener(dataCenterChangeListener);
      binding.dataCenterOversea.setOnClickListener(dataCenterChangeListener);
    }
  }

  private void logoutThenQuitApp(final Runnable onFailure) {
    AppStates.get().setAppRestartInFlight(true);
    toggleLoading(true);
    AuthorManager.INSTANCE.logout(
        new LoginCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void unused) {
            // ensure AuthManager clear user info cache
            new Handler()
                .postDelayed(
                    () -> {
                      //                      PstnUIHelper.destroy();
                      toggleLoading(false);
                      if (getActivity() != null) {
                        Intent intent =
                            getActivity()
                                .getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                      }
                      ActivityManager mActivityManager =
                          (ActivityManager)
                              requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
                      List<ActivityManager.RunningAppProcessInfo> mList =
                          mActivityManager.getRunningAppProcesses();
                      for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
                        if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                          android.os.Process.killProcess(runningAppProcessInfo.pid);
                        }
                      }
                      android.os.Process.killProcess(android.os.Process.myPid());
                      System.exit(0);
                    },
                    1500);
          }

          @Override
          public void onError(int i, @NonNull String s) {
            AppStates.get().setAppRestartInFlight(false);
            toggleLoading(false);
            onFailure.run();
          }
        });
  }

  private Dialog loadingDialog;

  private void toggleLoading(boolean show) {
    if (loadingDialog == null) {
      loadingDialog = new LoadingDialog(requireActivity());
    }
    if (show && !loadingDialog.isShowing()) {
      loadingDialog.show();
    } else if (!show) {
      loadingDialog.dismiss();
      loadingDialog = null;
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
    NERtcCallbackProxyMgr.getInstance().removeCallback(neRtcCallbackExTemp);
  }
}
